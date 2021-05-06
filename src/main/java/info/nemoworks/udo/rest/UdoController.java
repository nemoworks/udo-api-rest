package info.nemoworks.udo.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import info.nemoworks.udo.service.UdoService;

@Controller
public class UdoController {

    // @Autowired
    // private UdoService udoService;

    @GetMapping(value ="/")
    public void welcome(){

        // udoService.notify();
    }
    
}
