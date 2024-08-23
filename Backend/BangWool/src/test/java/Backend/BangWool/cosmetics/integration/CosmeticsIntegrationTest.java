package Backend.BangWool.cosmetics.integration;

import Backend.BangWool.cosmetics.domain.Category;
import Backend.BangWool.cosmetics.domain.CosmeticsEntity;
import Backend.BangWool.cosmetics.domain.LocationEntity;
import Backend.BangWool.cosmetics.dto.CosmeticsCreateRequest;
import Backend.BangWool.cosmetics.repository.CosmeticsRepository;
import Backend.BangWool.cosmetics.repository.LocationRepository;
import Backend.BangWool.cosmetics.service.CosmeticsService;
import Backend.BangWool.cosmetics.service.LocationService;
import Backend.BangWool.image.service.S3ImageService;
import Backend.BangWool.member.domain.MemberEntity;
import Backend.BangWool.member.dto.Session;
import Backend.BangWool.member.repository.MemberRepository;
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
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class CosmeticsIntegrationTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper objectMapper;

    @Autowired private LocationService locationService;
    @Autowired private CosmeticsService cosmeticsService;
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
        when(s3ImageService.upload(image, "cosmetics" + String.valueOf(session.getId()), 512, false)).thenReturn(URI.create("test.uri"));

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

    



}
