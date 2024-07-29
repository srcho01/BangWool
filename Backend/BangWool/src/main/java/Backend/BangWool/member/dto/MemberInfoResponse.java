package Backend.BangWool.member.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MemberInfoResponse {

    private int memberID;

    private String email;

    private String name;
    private String nickname;

    private LocalDate birth;

    private String googleId;
    private String kakaoId;

}
