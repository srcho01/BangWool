package Backend.BangWool.cosmetics.service;

import Backend.BangWool.cosmetics.domain.Category;
import Backend.BangWool.cosmetics.domain.CosmeticsEntity;
import Backend.BangWool.cosmetics.domain.LocationEntity;
import Backend.BangWool.cosmetics.dto.CosmeticsCreateRequest;
import Backend.BangWool.cosmetics.dto.CosmeticsInfoResponse;
import Backend.BangWool.cosmetics.repository.CosmeticsRepository;
import Backend.BangWool.exception.BadRequestException;
import Backend.BangWool.image.service.S3ImageService;
import Backend.BangWool.member.domain.MemberEntity;
import Backend.BangWool.member.dto.Session;
import Backend.BangWool.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;

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
            String filename = "cosmetics" + String.valueOf(session.getId());
            URI uri = s3ImageService.upload(image, filename, 512, true);
            cosmetics.setImage(uri);
        }

        // 리포지토리 저장
        member.addCosmetics(cosmetics);
        cosmeticsRepository.save(cosmetics);
        memberRepository.save(member);

        return makeCosmeticsInfoResponse(member, cosmetics, location);
    }


    private CosmeticsInfoResponse makeCosmeticsInfoResponse(MemberEntity member, CosmeticsEntity cosmetics, LocationEntity location) {
        return CosmeticsInfoResponse.builder()
                .id(cosmetics.getId())
                .memberId(member.getId())
                .memberEmail(member.getEmail())
                .name(cosmetics.getName())
                .category(cosmetics.getCategory())
                .expirationDate(cosmetics.getExpirationDate())
                .startDate(cosmetics.getStartDate())
                .status(cosmetics.getStatus())
                .locationName(location.getName())
                .image(cosmetics.getImage())
                .build();
    }

}
