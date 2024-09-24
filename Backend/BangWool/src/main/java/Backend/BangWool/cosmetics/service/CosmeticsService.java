package Backend.BangWool.cosmetics.service;

import Backend.BangWool.cosmetics.domain.Category;
import Backend.BangWool.cosmetics.domain.CosmeticsEntity;
import Backend.BangWool.cosmetics.domain.LocationEntity;
import Backend.BangWool.cosmetics.dto.CosmeticsCreateRequest;
import Backend.BangWool.cosmetics.dto.CosmeticsInfoResponse;
import Backend.BangWool.cosmetics.dto.CosmeticsUpdateRequest;
import Backend.BangWool.cosmetics.repository.CosmeticsRepository;
import Backend.BangWool.exception.BadRequestException;
import Backend.BangWool.exception.NotFoundException;
import Backend.BangWool.image.service.S3ImageService;
import Backend.BangWool.member.domain.MemberEntity;
import Backend.BangWool.member.dto.Session;
import Backend.BangWool.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CosmeticsService {

    private final LocationService locationService;
    private final S3ImageService s3ImageService;

    private final CosmeticsRepository cosmeticsRepository;
    private final MemberRepository memberRepository;


    @Transactional
    public CosmeticsInfoResponse create(Session session, CosmeticsCreateRequest request, MultipartFile image) {
        // category 검사
        if (!Category.contains(request.getCategory())) {
            throw new BadRequestException("IllegalArgumentException - Invalid category \"" + request.getCategory() + "\"");
        }

        // 위치 생성
        LocationEntity location = locationService.create(session, request.getLocation());

        // member 가져오기
        MemberEntity member = memberRepository.getReferenceById(session.getId());

        // 화장품 entity 생성
        CosmeticsEntity cosmetics = CosmeticsEntity.builder()
                .name(request.getName())
                .expirationDate(request.getExpirationDate())
                .category(Category.valueOf(request.getCategory()))
                .location(location)
                .build();

        // 사진 생성
        if (image != null && !image.isEmpty()) {
            String filename = "cosmetics" + session.getId();
            URI uri = s3ImageService.upload(image, filename, 512, false);
            cosmetics.setImage(uri);
        }

        // 리포지토리 저장
        member.addCosmetics(cosmetics);
        cosmeticsRepository.save(cosmetics);
        memberRepository.save(member);

        return setCosmeticsInfoResponse(member, cosmetics);
    }

    public Map<Integer, List<CosmeticsInfoResponse>> readByStatus(Session session) {
        MemberEntity member = getMember(session.getId());
        List<CosmeticsEntity> cosmeticsList = member.getCosmetics();

        Map<Integer, List<CosmeticsInfoResponse>> map = new HashMap<>();
        for (int i = 0; i < 3; i++) {
            map.put(i, new ArrayList<>());
        }

        for (CosmeticsEntity cosmetics: cosmeticsList) {
            CosmeticsInfoResponse response = setCosmeticsInfoResponse(member, cosmetics);
            int status = response.getStatus();
            map.get(status).add(response);
        }

        return map;
    }

    public Map<String, List<CosmeticsInfoResponse>> readByLocation(Session session) {
        MemberEntity member = getMember(session.getId());
        List<CosmeticsEntity> cosmeticsList = member.getCosmetics();

        Map<String, List<CosmeticsInfoResponse>> map = new HashMap<>();

        for (CosmeticsEntity cosmetics: cosmeticsList) {
            CosmeticsInfoResponse response = setCosmeticsInfoResponse(member, cosmetics);
            String locationName = response.getLocationName();

            if (!map.containsKey(locationName)) {
                map.put(locationName, new ArrayList<>());
            }
            map.get(locationName).add(response);
        }

        return map;
    }

    public CosmeticsInfoResponse update(Session session, CosmeticsUpdateRequest request, MultipartFile image) {
        MemberEntity member = getMember(session.getId());

        CosmeticsEntity cosmetics = cosmeticsRepository.findByMemberAndId(member, request.getId())
                .orElseThrow(() -> new NotFoundException("Cosmetics not found"));

        // image
        setCosmeticsImage(session.getId(), request.getImageStatus(), image, cosmetics);

        // name
        if (request.getName() != null && !request.getName().isEmpty()) {
            cosmetics.setName(request.getName());
        }

        // category
        setCategory(request.getCategory(), cosmetics);

        // location
        String prevLocationName = setLocation(session, request.getLocation(), cosmetics);

        // save cosmetics
        cosmeticsRepository.save(cosmetics);

        // 기존 위치 옵션의 참조가 0이라면 삭제
        if (prevLocationName != null) {
            locationService.delete(session, prevLocationName);
        }

        return setCosmeticsInfoResponse(member, cosmetics);
    }


    private void setCosmeticsImage(Long id, String status, MultipartFile image, CosmeticsEntity cosmetics) {
        if (status != null) {
            if (status.equals("UPDATE")) {
                if (image == null || image.isEmpty()) {
                    throw new BadRequestException("If you want to update the image, an image file is needed");
                }

                String filename = "cosmetics" + id;
                System.out.println("filename : " + filename);

                URI newImageUri = s3ImageService.upload(image, filename, 512, false);

                s3ImageService.delete(cosmetics.getImage());
                cosmetics.setImage(newImageUri);

            } else if (status.equals("DELETE")) {
                s3ImageService.delete(cosmetics.getImage());
                cosmetics.setImage(CosmeticsEntity.getDefaultImage(cosmetics.getCategory()));

            } else {
                throw new BadRequestException("Wrong image status : " + status);
            }
        }
    }

    private void setCategory(String category, CosmeticsEntity cosmetics) {
        if (category != null && !category.isEmpty()) {
            if (Category.contains(category)) {
                cosmetics.setCategory(Category.valueOf(category));
            } else {
                throw new BadRequestException("Invalid category - " + category);
            }
        }
    }

    private String setLocation(Session session, String locationName, CosmeticsEntity cosmetics) {
        String prev = null;

        if (locationName != null && !locationName.isEmpty()) {
            prev = cosmetics.getLocation().getName();

            // 변경할 옵션이 존재하지 않으면 생성하고 return, 존재하면 기존에 생성되어 있던 location return
            LocationEntity location = locationService.create(session, locationName);

            // set
            cosmetics.setLocation(location);
        }

        return prev;
    }


    private CosmeticsInfoResponse setCosmeticsInfoResponse(MemberEntity member, CosmeticsEntity cosmetics) {
        return CosmeticsInfoResponse.builder()
                .id(cosmetics.getId())
                .memberId(member.getId())
                .memberEmail(member.getEmail())
                .name(cosmetics.getName())
                .category(cosmetics.getCategory())
                .expirationDate(cosmetics.getExpirationDate())
                .startDate(cosmetics.getStartDate())
                .status(cosmetics.getStatus())
                .locationName(cosmetics.getLocation().getName())
                .image(cosmetics.getImage())
                .build();
    }

    private MemberEntity getMember(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

}
