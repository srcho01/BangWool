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
import Backend.BangWool.util.CONSTANT;
import Backend.BangWool.util.WithMockMember;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;

import java.net.URI;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
@WithMockMember
class CosmeticsServiceTest {

    @Autowired private CosmeticsService cosmeticsService;

    @MockBean private S3ImageService s3ImageService;
    @MockBean private LocationService locationService;
    @MockBean private MemberRepository memberRepository;
    @MockBean private CosmeticsRepository cosmeticsRepository;

    private Session session;
    private MemberEntity member;

    @BeforeEach
    void setUp() {
        this.member = MemberEntity.builder()
                .email("test@test.com")
                .password("test1234!!")
                .birth(LocalDate.of(2000, 1, 1))
                .name("test")
                .nickname("springtest").build();

        this.session = (Session) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }


    @DisplayName("화장품 생성 - 에러 : enum에 없는 카테고리")
    @Test
    void createFail() {
        // given
        CosmeticsCreateRequest request = CosmeticsCreateRequest.builder()
                .category("error")
                .build();

        // when & then
        BadRequestException e = assertThrows(BadRequestException.class, () -> cosmeticsService.create(session, request, null));
        assertThat(e.getMessage()).isEqualTo("IllegalArgumentException - Invalid category \"error\"");
    }

    @DisplayName("화장품 생성 - 성공 : 이미지 없음")
    @Test
    void createSuccess1() {
        // given
        CosmeticsCreateRequest request = CosmeticsCreateRequest.builder()
                .name("cosname")
                .expirationDate(LocalDate.of(2024, 8, 12))
                .category("color")
                .location("table")
                .build();

        LocationEntity location = LocationEntity.builder()
                .name("table")
                .build();

        CosmeticsEntity cosmetics = CosmeticsEntity.builder()
                .name("cosname")
                .expirationDate(LocalDate.of(2024, 8, 12))
                .location(location)
                .category(Category.color)
                .image(CONSTANT.DEFAULT_COS_COLOR)
                .build();

        // mocking
        when(locationService.create(session, "table")).thenReturn(location);
        when(memberRepository.getReferenceById(session.getId())).thenReturn(member);

        // when
        cosmeticsService.create(session, request, null);

        // then
        verify(locationService, times(1)).create(session, "table");
        verify(memberRepository, times(1)).getReferenceById(session.getId());
        verify(cosmeticsRepository, times(1)).save(cosmetics);

        member.addCosmetics(cosmetics);
        member.addLocation(location);
        verify(memberRepository, times(1)).save(member);
    }

    @DisplayName("화장품 생성 - 성공 : 이미지 있음")
    @Test
    void createSuccess2() {
        // given
        URI uri = CONSTANT.DEFAULT_PROFILE;

        CosmeticsCreateRequest request = CosmeticsCreateRequest.builder()
                .name("cosname")
                .expirationDate(LocalDate.of(2024, 8, 12))
                .category("color")
                .location("table")
                .build();

        LocationEntity location = LocationEntity.builder()
                .name("table")
                .build();

        CosmeticsEntity cosmetics = CosmeticsEntity.builder()
                .name("cosname")
                .expirationDate(LocalDate.of(2024, 8, 12))
                .location(location)
                .category(Category.color)
                .build();

        MockMultipartFile image = new MockMultipartFile(
                "test",
                "test-image.png",
                "image/jpeg",
                "fake image content".getBytes()
        );

        // mocking
        when(locationService.create(session, "table")).thenReturn(location);
        when(memberRepository.getReferenceById(session.getId())).thenReturn(member);
        when(cosmeticsRepository.save(cosmetics)).thenReturn(cosmetics);
        when(s3ImageService.upload(image, "test", 512, true)).thenReturn(uri);

        // when
        cosmeticsService.create(session, request, image);

        // then
        verify(locationService, times(1)).create(session, "table");
        verify(memberRepository, times(1)).getReferenceById(session.getId());
        verify(cosmeticsRepository, times(1)).save(any(CosmeticsEntity.class));
        verify(s3ImageService, times(1)).upload(image, "cosmetics" + session.getId(), 512, false);

        cosmetics.setImage(uri);
        member.addCosmetics(cosmetics);
        member.addLocation(location);
        verify(memberRepository, times(1)).save(member);
    }


    @DisplayName("화장품 상태별 읽기 - 성공 : 화장품 없음")
    @Test
    void readByStatusSuccess1() {
        // mocking
        when(memberRepository.findById(this.session.getId())).thenReturn(Optional.of(this.member));

        // when
        Map<Integer, List<CosmeticsInfoResponse>> result = cosmeticsService.readByStatus(this.session);

        // then
        Map<Integer, List<CosmeticsInfoResponse>> expected = new HashMap<>();
        for (int i = 0; i < 3; i++) {
            expected.put(i, new ArrayList<>());
        }
        assertThat(result).isEqualTo(expected);

    }

    @DisplayName("화장품 상태별 읽기 - 성공 : 화장품 있음")
    @Test
    void readByStatusSuccess2() {
        // given
        LocationEntity location = LocationEntity.builder()
                .name("location")
                .build();
        this.member.addLocation(location);

        CosmeticsEntity cosmetics1 = CosmeticsEntity.builder()
                .name("화장품1")
                .expirationDate(LocalDate.of(2000, 1, 1))
                .category(Category.valueOf("basic"))
                .location(location)
                .build();
        CosmeticsEntity cosmetics2 = CosmeticsEntity.builder()
                .name("화장품2")
                .expirationDate(LocalDate.of(2000, 1, 1))
                .category(Category.valueOf("basic"))
                .location(location)
                .build();
        CosmeticsEntity cosmetics3 = CosmeticsEntity.builder()
                .name("화장품3")
                .expirationDate(LocalDate.of(2000, 1, 1))
                .category(Category.valueOf("basic"))
                .location(location)
                .build();
        cosmetics3.setStatus(2);

        this.member.addCosmetics(cosmetics1);
        this.member.addCosmetics(cosmetics2);
        this.member.addCosmetics(cosmetics3);

        // mocking
        when(memberRepository.findById(this.session.getId())).thenReturn(Optional.of(this.member));

        // when
        Map<Integer, List<CosmeticsInfoResponse>> result = cosmeticsService.readByStatus(session);

        // then
        Map<Integer, List<CosmeticsInfoResponse>> expected = new HashMap<>();
        for (int i = 0; i < 3; i++) {
            expected.put(i, new ArrayList<>());
        }
        expected.get(0).add(setCosmeticsInfoResponse(this.member, cosmetics1));
        expected.get(0).add(setCosmeticsInfoResponse(this.member, cosmetics2));
        expected.get(2).add(setCosmeticsInfoResponse(this.member, cosmetics3));

        assertThat(result).isEqualTo(expected);
    }


    @DisplayName("화장품 위치별 읽기 - 성공 : 화장품 없음")
    @Test
    void readByLocationSuccess1() {
        // mocking
        when(memberRepository.findById(this.session.getId())).thenReturn(Optional.of(this.member));

        // when
        Map<String, List<CosmeticsInfoResponse>> result = cosmeticsService.readByLocation(this.session);

        // then
        Map<Integer, List<CosmeticsInfoResponse>> expected = new HashMap<>();
        assertThat(result).isEqualTo(expected);
    }

    @DisplayName("화장품 위치별 읽기 - 성공 : 화장품 있음")
    @Test
    void readByLocationSuccess2() {
        // given
        LocationEntity location1 = LocationEntity.builder()
                .name("location1")
                .build();
        LocationEntity location2 = LocationEntity.builder()
                .name("location2")
                .build();
        LocationEntity location3 = LocationEntity.builder()
                .name("location3")
                .build();
        this.member.addLocation(location1);
        this.member.addLocation(location2);
        this.member.addLocation(location3);

        CosmeticsEntity cosmetics1 = CosmeticsEntity.builder()
                .name("화장품")
                .expirationDate(LocalDate.of(2000, 1, 1))
                .category(Category.valueOf("basic"))
                .location(location1)
                .build();
        CosmeticsEntity cosmetics2 = CosmeticsEntity.builder()
                .name("화장품")
                .expirationDate(LocalDate.of(2000, 1, 1))
                .category(Category.valueOf("basic"))
                .location(location2)
                .build();
        CosmeticsEntity cosmetics3 = CosmeticsEntity.builder()
                .name("화장품")
                .expirationDate(LocalDate.of(2000, 1, 1))
                .category(Category.valueOf("basic"))
                .location(location3)
                .build();
        this.member.addCosmetics(cosmetics1);
        this.member.addCosmetics(cosmetics2);
        this.member.addCosmetics(cosmetics3);
        this.member.addCosmetics(cosmetics3);

        // mocking
        when(memberRepository.findById(this.session.getId())).thenReturn(Optional.of(this.member));

        // when
        Map<String, List<CosmeticsInfoResponse>> result = cosmeticsService.readByLocation(session);

        // then
        Map<String, List<CosmeticsInfoResponse>> expected = new HashMap<>();
        expected.put("location1", Arrays.asList(setCosmeticsInfoResponse(this.member, cosmetics1)));
        expected.put("location2", Arrays.asList(setCosmeticsInfoResponse(this.member, cosmetics2)));
        expected.put("location3", Arrays.asList(setCosmeticsInfoResponse(this.member, cosmetics3), setCosmeticsInfoResponse(this.member, cosmetics3)));

        assertThat(result).isEqualTo(expected);
        assertThat(result.get("location3").size()).isEqualTo(2);
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

}