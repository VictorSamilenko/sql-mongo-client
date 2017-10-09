package client.controller;

import client.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class ClientController {
    @Autowired
    ClientService service;

    @RequestMapping(value={"/"}, method={RequestMethod.GET})
    public String index() {
        return "index";
    }

    @RequestMapping(value={"/"}, method={RequestMethod.POST})
    public ResponseEntity<String> result(String sql) {
        String response;
        try {
            response = this.service.getResult(sql);
        }
        catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity(response, HttpStatus.OK);
    }
}
