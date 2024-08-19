package Backend.BangWool.cosmetics.integration;

import Backend.BangWool.cosmetics.dto.LocationUpdateRequest;
import Backend.BangWool.cosmetics.service.LocationService;
import Backend.BangWool.member.domain.MemberEntity;
import Backend.BangWool.member.dto.Session;
import Backend.BangWool.member.repository.MemberRepository;
import Backend.BangWool.response.DataResponse;
import Backend.BangWool.response.StatusResponse;
import Backend.BangWool.util.JWTUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class LocationIntegrationTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper objectMapper;

    @Autowired private LocationService locationService;
    @Autowired private MemberRepository memberRepository;

    @Autowired private JWTUtil jwtUtil;


    private Session session;
    private String jwt;

    @BeforeEach
    void setUp() {
        MemberEntity member = MemberEntity.builder()
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


    @DisplayName("위치 목록 조회 - 성공")
    @Test
    void readSuccess() throws Exception {
        // given
        List<String> optionList = List.of("option1", "option2", "option3");
        for (String option : optionList) {
            locationService.create(session, option);
        }

        // then
        String response = objectMapper.writeValueAsString(DataResponse.of(optionList));
        mvc.perform(get("/cosmetics/location/list")
                        .header("Authorization", "Bearer " + this.jwt))
                .andExpect(status().isOk())
                .andExpect(content().json(response));
    }


    @DisplayName("위치 이름 변경 - 실패")
    @Test
    void updateFail() throws Exception {
        // then
        String response = objectMapper.writeValueAsString(StatusResponse.of(400, "Message format is incorrect."));
        mvc.perform(patch("/cosmetics/location/rename")
                        .header("Authorization", "Bearer " + this.jwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(response));
    }

    @DisplayName("위치 이름 변경")
    @Test
    void updateSuccess() throws Exception {
        // given
        List<String> optionList = List.of("option1", "option2", "option3");
        for (String option : optionList) {
            locationService.create(session, option);
        }

        Map<String, String> options = new HashMap<>();
        options.put("option1", "OPTION1");
        options.put("option3", "OPTION3");
        LocationUpdateRequest request = LocationUpdateRequest.builder().options(options).build();
        String requestString = objectMapper.writeValueAsString(request);

        // then
        String response = objectMapper.writeValueAsString(DataResponse.of(List.of("OPTION1", "option2", "OPTION3")));
        mvc.perform(patch("/cosmetics/location/rename")
                        .header("Authorization", "Bearer " + this.jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestString))
                .andExpect(status().isOk())
                .andExpect(content().json(response));

    }

}
