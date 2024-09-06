package Backend.BangWool.cosmetics.integration;

import Backend.BangWool.cosmetics.domain.Category;
import Backend.BangWool.cosmetics.domain.CosmeticsEntity;
import Backend.BangWool.cosmetics.domain.LocationEntity;
import Backend.BangWool.cosmetics.dto.CosmeticsCreateRequest;
import Backend.BangWool.cosmetics.dto.CosmeticsInfoResponse;
import Backend.BangWool.cosmetics.repository.CosmeticsRepository;
import Backend.BangWool.cosmetics.repository.LocationRepository;
import Backend.BangWool.image.service.S3ImageService;
import Backend.BangWool.member.domain.MemberEntity;
import Backend.BangWool.member.dto.Session;
import Backend.BangWool.member.repository.MemberRepository;
import Backend.BangWool.response.DataResponse;
import Backend.BangWool.util.JWTUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class CosmeticsIntegrationTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private S3ImageService s3ImageService;

    @Autowired private MemberRepository memberRepository;
    @Autowired private LocationRepository locationRepository;
    @Autowired private CosmeticsRepository cosmeticsRepository;

    @Autowired private JWTUtil jwtUtil;

    private MemberEntity member;
    private Session session;
    private String jwt;

    @BeforeEach
    void setUp() {
        member = MemberEntity.builder()
                .email("test@test.com")
                .password("test1234!!")
                .birth(LocalDate.of(2000, 1, 1))
                .name("test")
                .nickname("springtest").build();
        memberRepository.save(member);

        this.session = Session.builder()
                .id(member.getId())
                .username(member.getEmail())
                .build();

        this.jwt = jwtUtil.generateToken("access", member.getId(), member.getEmail(), "ROLE_USER", 3600L);
    }


    private static Stream<Arguments> invalidCreate() {
        return Stream.of(
                Arguments.of(null, LocalDate.of(2000, 3, 11), "base", "위치"),
                Arguments.of("test", null, "base", "위치"),
                Arguments.of("test", LocalDate.of(2000, 3, 11), null, "위치"),
                Arguments.of("test", LocalDate.of(2000, 3, 11), "base", null)
        );
    }

    @DisplayName("화장품 생성 : 에러 - 필드값 오류")
    @ParameterizedTest
    @MethodSource("invalidCreate")
    void createFail(String name, LocalDate date, String category, String location) throws Exception {
        // given
        CosmeticsCreateRequest request = CosmeticsCreateRequest.builder()
                .name(name)
                .expirationDate(date)
                .category(category)
                .location(location)
                .build();
        MockMultipartFile partRequest = new MockMultipartFile(
                "data",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(request)
        );

        // then
        mvc.perform(multipart("/cosmetics/create")
                        .file(partRequest)
                        .header("Authorization", "Bearer " + this.jwt)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @DisplayName("화장품 생성 : 성공 - 이미지 없음")
    @Test
    void createSuccess1() throws Exception {
        // given
        CosmeticsCreateRequest request = CosmeticsCreateRequest.builder()
                .name("test")
                .expirationDate(LocalDate.of(2000, 1, 1))
                .category("base")
                .location("위치")
                .build();
        MockMultipartFile partRequest = new MockMultipartFile(
                "data",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(request)
        );

        // when
        mvc.perform(multipart("/cosmetics/create")
                        .file(partRequest)
                        .header("Authorization", "Bearer " + this.jwt)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());

        // then
        LocationEntity location = LocationEntity.builder().name("위치").build();
        CosmeticsEntity cosmetics = CosmeticsEntity.builder()
                .name("test")
                .expirationDate(LocalDate.of(2000, 1, 1))
                .category(Category.base)
                .location(location)
                .build();
        member.addLocation(location);
        member.addCosmetics(cosmetics);

        MemberEntity memberResult = memberRepository.findById(session.getId()).orElseThrow();
        LocationEntity locationResult = memberResult.getLocationOptions().getFirst();
        CosmeticsEntity cosmeticsResult = memberResult.getCosmetics().getFirst();

        assertThat(memberResult).isEqualTo(member);
        assertThat(locationResult.getName()).isEqualTo(location.getName());
        assertThat(cosmeticsResult).isEqualTo(cosmetics);
    }

    @DisplayName("화장품 생성 : 성공 - 이미지 있음")
    @Test
    void createSuccess2() throws Exception {
        // given
        CosmeticsCreateRequest request = CosmeticsCreateRequest.builder()
                .name("test")
                .expirationDate(LocalDate.of(2000, 1, 1))
                .category("base")
                .location("위치")
                .build();
        MockMultipartFile partRequest = new MockMultipartFile(
                "data",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(request)
        );
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "",
                "image/jpeg",
                "fake image content".getBytes()
        );

        // mocking
        when(s3ImageService.upload(image, "cosmetics" + session.getId(), 512, false)).thenReturn(URI.create("test.uri"));

        // when
        mvc.perform(multipart("/cosmetics/create")
                        .file(partRequest)
                        .file(image)
                        .header("Authorization", "Bearer " + this.jwt)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());

        // then
        LocationEntity location = LocationEntity.builder().name("위치").build();
        CosmeticsEntity cosmetics = CosmeticsEntity.builder()
                .name("test")
                .expirationDate(LocalDate.of(2000, 1, 1))
                .category(Category.base)
                .location(location)
                .image(URI.create("test.uri"))
                .build();
        member.addLocation(location);
        member.addCosmetics(cosmetics);

        MemberEntity memberResult = memberRepository.findById(session.getId()).orElseThrow();
        LocationEntity locationResult = memberResult.getLocationOptions().getFirst();
        CosmeticsEntity cosmeticsResult = memberResult.getCosmetics().getFirst();

        assertThat(memberResult).isEqualTo(member);
        assertThat(locationResult.getName()).isEqualTo(location.getName());
        assertThat(cosmeticsResult).isEqualTo(cosmetics);
    }


    @DisplayName("화장품 상태별 조회 : 성공")
    @Test
    void readByStatusSuccess() throws Exception {
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

        locationRepository.save(location);
        cosmeticsRepository.save(cosmetics1);
        cosmeticsRepository.save(cosmetics2);
        cosmeticsRepository.save(cosmetics3);
        memberRepository.save(member);

        // when & then
        Map<Integer, List<CosmeticsInfoResponse>> expected = new HashMap<>();
        for (int i = 0; i < 3; i++) {
            expected.put(i, new ArrayList<>());
        }
        expected.get(0).add(setCosmeticsInfoResponse(this.member, cosmetics1));
        expected.get(0).add(setCosmeticsInfoResponse(this.member, cosmetics2));
        expected.get(2).add(setCosmeticsInfoResponse(this.member, cosmetics3));

        String responseJson = objectMapper.writeValueAsString(DataResponse.of(expected));

        mvc.perform(get("/cosmetics/read/status")
                        .header("Authorization", "Bearer " + this.jwt))
                .andExpect(status().isOk())
                .andExpect(content().json(responseJson));
    }


    @DisplayName("화장품 위치별 조회 : 성공")
    @Test
    void readByLocationSuccess() throws Exception {
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

        locationRepository.save(location1);
        locationRepository.save(location2);
        locationRepository.save(location3);
        cosmeticsRepository.save(cosmetics1);
        cosmeticsRepository.save(cosmetics2);
        cosmeticsRepository.save(cosmetics3);
        cosmeticsRepository.save(cosmetics3);
        memberRepository.save(member);

        // when & then
        Map<String, List<CosmeticsInfoResponse>> expected = new HashMap<>();
        expected.put("location1", Arrays.asList(setCosmeticsInfoResponse(this.member, cosmetics1)));
        expected.put("location2", Arrays.asList(setCosmeticsInfoResponse(this.member, cosmetics2)));
        expected.put("location3", Arrays.asList(setCosmeticsInfoResponse(this.member, cosmetics3), setCosmeticsInfoResponse(this.member, cosmetics3)));

        String responseJson = objectMapper.writeValueAsString(DataResponse.of(expected));

        mvc.perform(get("/cosmetics/read/location")
                        .header("Authorization", "Bearer " + this.jwt))
                .andExpect(status().isOk())
                .andExpect(content().json(responseJson));
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
