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
            exportItemService.saveExportItems(items, user,ExportItem.Category.INRUSH.getValue());
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("设置成功", "")).body(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", "设置失败")).body(null);
        }
    }

    @GetMapping("/getExportItems")
    @ApiOperation(value = "查询导出项", notes = "查询导出项")
    public ResponseEntity<ItemsModel> getExportItems(@RequestHeader(value = "X-UserToken") String token) {
        try {
            User user = getUserByToken(token);
            ItemsModel result = exportItemService.getExportItems(user,ExportItem.Category.INRUSH.getValue());
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
            if (categoryOutsource.equals(items.getCategory())) {
                exportItemService.saveExportItems(items, user, categoryOutsource);
            } else {
                exportItemService.saveExportItems(items, user, categoryFollowup);
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
            ItemsModel result = null;

            if (categoryOutsource.equals(category)) {
                //委外案件导出项
                result = exportItemService.getExportItems(user, categoryOutsource);
            } else {
                //委外跟踪记录导出项
                result = exportItemService.getExportItems(user, categoryFollowup);
            }
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("查询成功", "")).body(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", "查询失败")).body(null);
        }
    }
}
