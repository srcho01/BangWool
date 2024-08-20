package Backend.BangWool.cosmetics.service;

import Backend.BangWool.cosmetics.domain.Category;
import Backend.BangWool.cosmetics.domain.CosmeticsEntity;
import Backend.BangWool.cosmetics.domain.LocationEntity;
import Backend.BangWool.cosmetics.dto.LocationUpdateRequest;
import Backend.BangWool.cosmetics.repository.LocationRepository;
import Backend.BangWool.exception.BadRequestException;
import Backend.BangWool.member.domain.MemberEntity;
import Backend.BangWool.member.dto.Session;
import Backend.BangWool.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class LocationServiceTest {

    @Autowired private MemberRepository memberRepository;
    @Autowired private LocationRepository locationRepository;

    @Autowired LocationService locationService;

    private MemberEntity member;
    private Session session;

    @BeforeEach
    void setUp() {
        this.member = MemberEntity.builder()
                .email("test@test.com")
                .password("test1234!!")
                .birth(LocalDate.of(2000, 1, 1))
                .name("test")
                .nickname("springtest").build();
        memberRepository.save(this.member);

        this.session = Session.builder()
                .id(this.member.getId())
                .username(this.member.getEmail())
                .build();
    }


    @DisplayName("위치 옵션 생성 - 에러 : 이름이 너무 긺")
    @Test
    void createFail1() {
        // given
        String name = "12345678901234567890";

        // when & then
        BadRequestException e = assertThrows(BadRequestException.class, () -> locationService.create(session, name));
        assertThat(e.getMessage()).isEqualTo("Location name should be between 1 and 10 characters");
    }

    @DisplayName("위치 옵션 생성 - 실패 : 이미 존재하는 옵션")
    @Test
    void createFail2() {
        // given
        String name = "123456";

        // when
        locationService.create(session, name);
        locationService.create(session, name);

        // then
        MemberEntity afterMember = memberRepository.findById(session.getId()).orElseThrow();
        int numLocOpts = afterMember.getLocationOptions().size();
        assertThat(numLocOpts).isEqualTo(1);
    }

    @DisplayName("위치 옵션 생성 - 성공")
    @Test
    void createSuccess() {
        // given
        String name = "123456";

        // when
        LocationEntity result = locationService.create(session, name);

        // then
        assertThat(result.getName()).isEqualTo(name);

        LocationEntity location = member.getLocationOptions().getFirst();
        assertThat(location.getMember()).isEqualTo(member);
        assertThat(location.getName()).isEqualTo(name);
    }


    @DisplayName("위치 옵션 읽기 - 성공")
    @Test
    void readSuccess() {
        // given
        List<String> names = List.of("test1", "test2", "test3");
        List<LocationEntity> locations = names.stream()
                .map(name -> LocationEntity.builder().name(name).build())
                .toList();
        for (LocationEntity location : locations) {
            member.addLocation(location);
        }

        // when
        List<String> locationNameList = locationService.read(session);

        // then
        Set<String> givenSet = new HashSet<>(names);
        Set<String> resultSet = new HashSet<>(locationNameList);
        assertThat(givenSet).isEqualTo(resultSet);
    }


    @DisplayName("위치 옵션 수정 - 성공")
    @Test
    void updateSuccess() {
        // given
        Map<String, String> updateOptions = Map.of(
                "option1", "OPTION1",
                "option2", "OPTION2",
                "option3", "OPTION3"
        );
        LocationUpdateRequest request = LocationUpdateRequest.builder().options(updateOptions).build();

        for (String option : updateOptions.keySet()) {
            LocationEntity location = LocationEntity.builder().name(option).build();
            member.addLocation(location);
            locationRepository.save(location);
        }

        memberRepository.save(member);

        // when
        List<String> result = locationService.update(session, request);

        // then
        Set<String> givenSet = new HashSet<>(updateOptions.values());
        Set<String> resultSet = new HashSet<>(result);
        assertThat(givenSet).isEqualTo(resultSet);
    }


    @DisplayName("위치 옵션 삭제 - 실패 : 없는 옵션")
    @Test
    void deleteFail1() {
        // given
        String option = "notfound";

        // when
        boolean result = locationService.delete(session, option);

        // then
        assertThat(result).isFalse();
    }

    @DisplayName("위치 옵션 삭제 - 실패 : cosmetics의 참조가 0이 아님")
    @Test
    void deleteFail2() {
        // given
        String option = "test";
        LocationEntity location = LocationEntity.builder().name(option).build();
        CosmeticsEntity cosmetics = CosmeticsEntity.builder()
                .name(option)
                .expirationDate(LocalDate.of(2000, 1, 1))
                .category(Category.base)
                .location(location)
                .build();

        member.addLocation(location);
        member.addCosmetics(cosmetics);

        locationRepository.save(location);
        memberRepository.save(member);

        // when
        boolean result = locationService.delete(session, option);

        // then
        assertThat(result).isFalse();
    }

    @DisplayName("위치 옵션 삭제 - 성공")
    @Test
    void deleteSuccess() {
        // given
        String option = "test";
        LocationEntity location = LocationEntity.builder().name(option).build();

        member.addLocation(location);

        locationRepository.save(location);
        memberRepository.save(member);

        // when
        boolean result = locationService.delete(session, option);

        // then
        boolean isInMemberLocation = member.getLocationOptions().stream()
                .anyMatch(loc -> loc.getName().equals(option));
        boolean isInLocationRepo = locationRepository.existsById(location.getId());

        assertThat(result).isTrue();
        assertThat(isInMemberLocation).isFalse();
        assertThat(isInLocationRepo).isFalse();
    }

}