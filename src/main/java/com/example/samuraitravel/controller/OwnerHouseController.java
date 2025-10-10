package com.example.samuraitravel.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/owner/house")
public class OwnerHouseController {
	//private final HouseService houseService;
	
	//public OwnerHouseController(HouseService houseService) {
	//	this.houseService = houseService;
	//}
	
	@GetMapping
	public String index() //(@RequestParam(name= "keyword", required = false) String keyword,
						//@PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable, Model model) 
	{
		//Page<House> housePage;
		
		//if (keyword != null && !keyword.isEmpty()) {
		//	housePage = houseService.findHousesByNameLike(keyword, pageable);
		//} else {
		//	housePage = houseService.findAllHouses(pageable);
		//}
		
		//model.addAttribute("housePage",housePage);
		//model.addAttribute("keyword", keyword);
		
		return "owner/house/index";
	}
}
