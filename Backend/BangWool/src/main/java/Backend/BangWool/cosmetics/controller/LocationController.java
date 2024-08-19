package Backend.BangWool.cosmetics.controller;

import Backend.BangWool.cosmetics.dto.LocationUpdateRequest;
import Backend.BangWool.cosmetics.service.LocationService;
import Backend.BangWool.member.dto.Session;
import Backend.BangWool.response.DataResponse;
import Backend.BangWool.util.CurrentSession;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name="Cosmetics Location", description = "화장품 위치 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/cosmetics/location")
public class LocationController {

    private final LocationService locationService;


    @Operation(summary = "사용자별 화장품 위치 목록 조회", description = "Response : 위치 목록 이름을 담은 리스트")
    @GetMapping("list")
    public DataResponse<List<String>> read(@CurrentSession Session session) {
        List<String> response = locationService.read(session);
        return DataResponse.of(response);
    }

    @Operation(summary = "사용자별 화장품 위치 이름 변경", description = """
            Response : 위치 목록 이름을 담은 리스트 <br><br>
            이름 변경할 것만 보내면 됩니다. 안 보낸 옵션, 없는 옵션은 이름 바뀌지 않습니다.""")
    @PatchMapping("rename")
    public DataResponse<List<String>> update(@CurrentSession Session session,
                                             @RequestBody LocationUpdateRequest request) {

        List<String> response = locationService.update(session, request);
        return DataResponse.of(response);
    }

}
