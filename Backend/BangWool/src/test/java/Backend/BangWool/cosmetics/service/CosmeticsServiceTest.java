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
import org.springframework.web.multipart.MultipartFile;

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


    @DisplayName("화장품 수정 - 오류 : 없는 화장품")
    @Test
    void updateFail1() {
        // given
        LocationEntity location = LocationEntity.builder()
                .name("location")
                .build();
        this.member.addLocation(location);

        CosmeticsEntity cosmetics = CosmeticsEntity.builder()
                .name("화장품")
                .expirationDate(LocalDate.of(2000, 1, 1))
                .category(Category.valueOf("basic"))
                .location(location)
                .build();
        this.member.addCosmetics(cosmetics);

        CosmeticsUpdateRequest request = CosmeticsUpdateRequest.builder()
                .id(23405L)
                .name("화장품")
                .category("basic")
                .build();

        // mocking
        when(memberRepository.findById(1L)).thenReturn(Optional.of(this.member));
        when(cosmeticsRepository.findByMemberAndId(this.member, 23405L)).thenReturn(Optional.empty());

        // when & then
        NotFoundException e = assertThrows(NotFoundException.class, () -> cosmeticsService.update(this.session, request, null));
        assertThat(e.getMessage()).isEqualTo("Cosmetics not found");
    }

    @DisplayName("화장품 수정 - 오류 : 이미지 업데이트 시 이미지 없음")
    @Test
    void updateFail2() {
        // given
        LocationEntity location = LocationEntity.builder()
                .name("location")
                .build();
        this.member.addLocation(location);

        CosmeticsEntity cosmetics = CosmeticsEntity.builder()
                .name("화장품")
                .expirationDate(LocalDate.of(2000, 1, 1))
                .category(Category.valueOf("basic"))
                .location(location)
                .build();
        this.member.addCosmetics(cosmetics);

        CosmeticsUpdateRequest request = CosmeticsUpdateRequest.builder()
                .id(23405L)
                .name("화장품")
                .category("basic")
                .imageStatus("UPDATE")
                .build();

        // mocking
        when(memberRepository.findById(1L)).thenReturn(Optional.of(this.member));
        when(cosmeticsRepository.findByMemberAndId(this.member, 23405L)).thenReturn(Optional.ofNullable(cosmetics));

        // when & then
        BadRequestException e = assertThrows(BadRequestException.class, () -> cosmeticsService.update(this.session, request, null));
        assertThat(e.getMessage()).isEqualTo("If you want to update the image, an image file is needed");
    }

    @DisplayName("화장품 수정 - 오류 : 존재하지 않는 카테고리")
    @Test
    void updateFail3() {
        // given
        LocationEntity location = LocationEntity.builder()
                .name("location")
                .build();
        this.member.addLocation(location);

        CosmeticsEntity cosmetics = CosmeticsEntity.builder()
                .name("화장품")
                .expirationDate(LocalDate.of(2000, 1, 1))
                .category(Category.valueOf("basic"))
                .location(location)
                .build();
        this.member.addCosmetics(cosmetics);

        CosmeticsUpdateRequest request = CosmeticsUpdateRequest.builder()
                .id(23405L)
                .name("화장품")
                .category("none")
                .build();

        // mocking
        when(memberRepository.findById(1L)).thenReturn(Optional.of(this.member));
        when(cosmeticsRepository.findByMemberAndId(this.member, 23405L)).thenReturn(Optional.ofNullable(cosmetics));

        // when & then
        BadRequestException e = assertThrows(BadRequestException.class, () -> cosmeticsService.update(this.session, request, null));
        assertThat(e.getMessage()).isEqualTo("Invalid category - none");
    }

    @DisplayName("화장품 수정 - 오류 : 잘못된 이미지 status")
    @Test
    void updateFail4() {
        // given
        LocationEntity location = LocationEntity.builder()
                .name("location")
                .build();
        this.member.addLocation(location);

        CosmeticsEntity cosmetics = CosmeticsEntity.builder()
                .name("화장품")
                .expirationDate(LocalDate.of(2000, 1, 1))
                .category(Category.valueOf("basic"))
                .location(location)
                .build();
        this.member.addCosmetics(cosmetics);

        CosmeticsUpdateRequest request = CosmeticsUpdateRequest.builder()
                .id(23405L)
                .name("화장품")
                .category("basic")
                .imageStatus("wrongImageStatus")
                .build();

        // mocking
        when(memberRepository.findById(1L)).thenReturn(Optional.of(this.member));
        when(cosmeticsRepository.findByMemberAndId(this.member, 23405L)).thenReturn(Optional.ofNullable(cosmetics));

        // when & then
        BadRequestException e = assertThrows(BadRequestException.class, () -> cosmeticsService.update(this.session, request, null));
        assertThat(e.getMessage()).isEqualTo("Wrong image status : wrongImageStatus");
    }

    @DisplayName("화장품 수정 - 성공 : 이미지 삭제")
    @Test
    void updateSuccess1() {
        // given
        LocationEntity location = LocationEntity.builder()
                .name("location")
                .build();
        LocationEntity newLocation = LocationEntity.builder()
                .name("newLocation")
                .build();
        this.member.addLocation(location);

        CosmeticsEntity cosmetics = CosmeticsEntity.builder()
                .name("name")
                .expirationDate(LocalDate.of(2000, 1, 1))
                .category(Category.valueOf("basic"))
                .location(location)
                .build();
        this.member.addCosmetics(cosmetics);

        CosmeticsUpdateRequest request = CosmeticsUpdateRequest.builder()
                .id(23405L)
                .name("newName")
                .category("color")
                .location("newLocation")
                .imageStatus("DELETE")
                .build();

        // mocking
        when(memberRepository.findById(1L)).thenReturn(Optional.of(this.member));
        when(cosmeticsRepository.findByMemberAndId(this.member, 23405L)).thenReturn(Optional.ofNullable(cosmetics));
        doNothing().when(s3ImageService).delete(cosmetics.getImage());
        when(locationService.create(this.session, "newLocation")).thenReturn(newLocation);
        when(locationService.delete(this.session,"location")).thenReturn(true);

        // when
        CosmeticsInfoResponse response = cosmeticsService.update(this.session, request, null);

        // then
        CosmeticsInfoResponse expected = CosmeticsInfoResponse.builder()
                .id(null)
                .memberId(this.member.getId())
                .memberEmail(this.member.getEmail())
                .name("newName")
                .category(Category.color)
                .expirationDate(cosmetics.getExpirationDate())
                .startDate(cosmetics.getStartDate())
                .status(cosmetics.getStatus())
                .locationName("newLocation")
                .image(CONSTANT.DEFAULT_COS_COLOR)
                .build();

        assertThat(response).isEqualTo(expected);
    }

    @DisplayName("화장품 수정 - 성공 : 이미지 업데이트")
    @Test
    void updateSuccess2() {
        // given
        LocationEntity location = LocationEntity.builder()
                .name("location")
                .build();
        this.member.addLocation(location);

        CosmeticsEntity cosmetics = CosmeticsEntity.builder()
                .name("name")
                .expirationDate(LocalDate.of(2000, 1, 1))
                .category(Category.valueOf("basic"))
                .location(location)
                .build();
        this.member.addCosmetics(cosmetics);

        CosmeticsUpdateRequest request = CosmeticsUpdateRequest.builder()
                .id(23405L)
                .imageStatus("UPDATE")
                .build();

        MultipartFile mockImage = mock(MultipartFile.class);

        // mocking
        when(memberRepository.findById(1L)).thenReturn(Optional.of(this.member));
        when(cosmeticsRepository.findByMemberAndId(this.member, 23405L)).thenReturn(Optional.ofNullable(cosmetics));
        when(s3ImageService.upload(mockImage, "cosmetics1", 512, false)).thenReturn(URI.create("https://cosmetics1.com"));
        doNothing().when(s3ImageService).delete(cosmetics.getImage());

        // when
        CosmeticsInfoResponse response = cosmeticsService.update(this.session, request, mockImage);

        // then
        CosmeticsInfoResponse expected = CosmeticsInfoResponse.builder()
                .id(null)
                .memberId(this.member.getId())
                .memberEmail(this.member.getEmail())
                .name(cosmetics.getName())
                .category(cosmetics.getCategory())
                .expirationDate(cosmetics.getExpirationDate())
                .startDate(cosmetics.getStartDate())
                .status(cosmetics.getStatus())
                .locationName(cosmetics.getLocation().getName())
                .image(URI.create("https://cosmetics1.com"))
                .build();

        assertThat(response).isEqualTo(expected);
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