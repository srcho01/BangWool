package Backend.BangWool.cosmetics.controller;

import Backend.BangWool.cosmetics.dto.CosmeticsCreateRequest;
import Backend.BangWool.cosmetics.dto.CosmeticsInfoResponse;
import Backend.BangWool.cosmetics.service.CosmeticsService;
import Backend.BangWool.member.dto.Session;
import Backend.BangWool.response.DataResponse;
import Backend.BangWool.util.CurrentSession;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name="Cosmetics", description = "화장품 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/cosmetics")
public class CosmeticsController {

    private final CosmeticsService cosmeticsService;


    @Operation(summary = "화장품 생성", description = """
            ※ Swagger Try it out 불가, Postman 가능 ※ <br><br>
            data(required)의 Content-type : application/json <br>
            image(not required)의 Content-type : multipart/form-data <br><br>
            data와 image 각각 Content-type 지정하여 전송 <br><br><br>
            ※ category 종류는 반드시 아래 중 하나 ※ <br><br>
            basic, base, color, others
            """)
    @PostMapping(value = "create")
    public DataResponse<CosmeticsInfoResponse> create(@CurrentSession Session session,
                                                      @RequestPart(value = "data") @Valid CosmeticsCreateRequest request,
                                                      @RequestPart(value = "image", required = false) MultipartFile image) {

        CosmeticsInfoResponse response = cosmeticsService.create(session, request, image);
        return DataResponse.of(response);
    }

}
