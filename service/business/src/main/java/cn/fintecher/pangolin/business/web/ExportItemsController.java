package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.ItemsModel;
import cn.fintecher.pangolin.business.repository.ExportItemRepository;
import cn.fintecher.pangolin.business.service.ExportItemService;
import cn.fintecher.pangolin.entity.ExportItem;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.web.HeaderUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.models.auth.In;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;



/**
 * Created by Administrator on 2017/9/26.
 */
@RestController
@RequestMapping("/api/exportItemsController")
@Api(value = "exportItemsController", description = "内催导出项")
public class ExportItemsController extends BaseController {

    @Inject
    ExportItemRepository exportItemRepository;
    @Inject
    ExportItemService exportItemService;

    @PostMapping("/saveExportItems")
    @ApiOperation(value = "设置导出项", notes = "设置导出项")
    public ResponseEntity saveExportItems(@RequestBody @ApiParam(value = "导出项", required = true) ItemsModel items,
                                          @RequestHeader(value = "X-UserToken") String token) {
        try {
            User user = getUserByToken(token);
            Integer categoryCaseInfo = ExportItem.Category.INRUSH.getValue();
            Integer categoryCaseInfoClosed = ExportItem.Category.INNERCLOSEDFOLLOWUP.getValue();
            if(categoryCaseInfo.equals(items.getCategory())){
                //设置内催导出项
                exportItemService.saveExportItems(items, user,categoryCaseInfo);
            }else {
                //设置内催已结案导出项
                exportItemService.saveExportItems(items, user,categoryCaseInfoClosed);
            }
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("设置成功", "")).body(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", "设置失败")).body(null);
        }
    }

    @GetMapping("/getExportItems")
    @ApiOperation(value = "查询导出项", notes = "查询导出项")
    public ResponseEntity<ItemsModel> getExportItems(@RequestParam @ApiParam(value = "导出项类型") Integer category,
                                                     @RequestHeader(value = "X-UserToken") String token) {
        try {
            User user = getUserByToken(token);
            ItemsModel result = null;
            Integer categoryCaseInfo = ExportItem.Category.INRUSH.getValue();
            Integer categoryCaseInfoClosed = ExportItem.Category.INNERCLOSEDFOLLOWUP.getValue();
            if(categoryCaseInfo.equals(category)){
                //查询内催导出项
                result = exportItemService.getExportItems(user,categoryCaseInfo);
            }else {
                //查询内催已结案导出项
                result = exportItemService.getExportItems(user,categoryCaseInfoClosed);
            }
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("查询成功", "")).body(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", "查询失败")).body(null);
        }
    }

    @PostMapping("/saveUpdateItems")
    @ApiOperation(value = "设置更新项", notes = "设置更新项")
    public ResponseEntity saveUpdateItems(@RequestBody @ApiParam(value = "更新项", required = true) ItemsModel items,
                                          @RequestHeader(value = "X-UserToken") String token) {
        try {
            User user = getUserByToken(token);
            exportItemService.saveExportItems(items, user,ExportItem.Category.CASEUPDATE.getValue());
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("设置成功", "")).body(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", "设置失败")).body(null);
        }
    }

    @GetMapping("/getUpdateItems")
    @ApiOperation(value = "查询更新项", notes = "查询更新项")
    public ResponseEntity<ItemsModel> getUpdateItems(@RequestHeader(value = "X-UserToken") String token) {
        try {
            User user = getUserByToken(token);
            ItemsModel result = exportItemService.getExportItems(user,ExportItem.Category.CASEUPDATE.getValue());
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("查询成功", "")).body(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", "查询失败")).body(null);
        }
    }

    @PostMapping("/saveOutsourceExportItems")
    @ApiOperation(value = "设置委外导出项", notes = "设置委外导出项")
    public ResponseEntity saveOutsourceExportItems(@RequestBody @ApiParam(value = "导出项", required = true) ItemsModel items,
                                          @RequestHeader(value = "X-UserToken") String token) {
        try {
            User user = getUserByToken(token);
            Integer categoryOutsource = ExportItem.Category.OUTSOURCE.getValue();
            Integer categoryFollowup = ExportItem.Category.OUTSOURCEFOLLOWUP.getValue();
            Integer categoryClosedFollowup = ExportItem.Category.OUTSOURCECLOSEDFOLLOWUP.getValue();
            if (categoryOutsource.equals(items.getCategory())) {
                //设置委外案件导出项
                exportItemService.saveExportItems(items, user, categoryOutsource);
            } else if(categoryFollowup.equals(items.getCategory())) {
                //设置委外催收中跟踪记录导出项
                exportItemService.saveExportItems(items, user, categoryFollowup);
            } else {
                //设置委外已结案跟踪记录导出项
                exportItemService.saveExportItems(items, user, categoryClosedFollowup);
            }

            return ResponseEntity.ok().headers(HeaderUtil.createAlert("设置成功", "")).body(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", "设置失败")).body(null);
        }
    }

    @GetMapping("/getOutsourceExportItems")
    @ApiOperation(value = "查询委外导出项", notes = "查询委外导出项")
    public ResponseEntity<ItemsModel> getOutsourceExportItems(@RequestHeader(value = "X-UserToken") String token, Integer category) {
        try {
            User user = getUserByToken(token);
            Integer categoryOutsource = ExportItem.Category.OUTSOURCE.getValue();
            Integer categoryFollowup = ExportItem.Category.OUTSOURCEFOLLOWUP.getValue();
            Integer categoryClosedFollowup = ExportItem.Category.OUTSOURCECLOSEDFOLLOWUP.getValue();
            ItemsModel result = null;
            if (categoryOutsource.equals(category)) {
                //委外案件导出项
                result = exportItemService.getExportItems(user, categoryOutsource);
            } else if(categoryFollowup.equals(category)) {
                //委外催收中跟踪记录导出项
                result = exportItemService.getExportItems(user, categoryFollowup);
            }else {
                //委外已结案跟踪记录导出项
                result = exportItemService.getExportItems(user, categoryClosedFollowup);
            }
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("查询成功", "")).body(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", "查询失败")).body(null);
        }
    }
}
