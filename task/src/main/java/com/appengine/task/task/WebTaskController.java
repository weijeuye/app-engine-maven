package com.appengine.task.task;

import com.appengine.task.domain.Task;
import com.appengine.task.service.TaskService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 任务界面跳转Controller
 * <p>
 * Authors: sofn
 * Version: 1.0  Created at 2015-10-22 00:11.
 */
@Controller
@RequestMapping("/web/task")
public class WebTaskController {

    @Resource
    private TaskService taskService;

    @RequestMapping(method = RequestMethod.GET)
    public String list() {
        return "task/list";
    }

    @RequestMapping(value = "save", method = RequestMethod.GET)
    public String save(
            @RequestParam(required = false, defaultValue = "0") long id,
            Model model
    ) {
        Task task = null;
        if (id > 0) {
            task = taskService.getTask(id);
        }
        model.addAttribute("task", task);
        return "task/save";
    }

}
