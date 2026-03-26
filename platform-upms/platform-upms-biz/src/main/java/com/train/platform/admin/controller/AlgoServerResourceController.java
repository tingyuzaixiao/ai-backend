package com.train.platform.admin.controller;

import com.train.platform.admin.api.entity.AlgoServerResource;
import com.train.platform.admin.service.AlgoServerResourceService;
import com.train.platform.common.core.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/algoserver")
@Tag(description = "算法服务", name = "算法服务")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class AlgoServerResourceController {
	private final AlgoServerResourceService algoService;

	@Operation(summary = "分页查询", description = "分页查询")
	@GetMapping("/page")
	public R getAlgoServerResourcePage(@RequestParam("current") Integer current,
									   @RequestParam("size") Integer size,
									   AlgoServerResource algoServerResource) {
		return R.ok(algoService.getAlgoServerResourcePage(current, size, algoServerResource));
	}
}
