package Backend.BangWool.cosmetics.service;

import Backend.BangWool.cosmetics.domain.Category;
import Backend.BangWool.cosmetics.domain.CosmeticsEntity;
import Backend.BangWool.cosmetics.domain.LocationEntity;
import Backend.BangWool.cosmetics.dto.CosmeticsCreateRequest;
import Backend.BangWool.cosmetics.dto.CosmeticsInfoResponse;
import Backend.BangWool.cosmetics.repository.CosmeticsRepository;
import Backend.BangWool.exception.BadRequestException;
import Backend.BangWool.exception.NotFoundException;
import Backend.BangWool.image.service.S3ImageService;
import Backend.BangWool.member.domain.MemberEntity;
import Backend.BangWool.member.dto.Session;
import Backend.BangWool.member.repository.MemberRepository;
import Backend.BangWool.util.CurrentSession;
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

    public Map<Integer, List<CosmeticsInfoResponse>> readByStatus(@CurrentSession Session session) {
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

    public Map<String, List<CosmeticsInfoResponse>> readByLocation(@CurrentSession Session session) {
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
