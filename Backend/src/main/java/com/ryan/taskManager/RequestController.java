package com.ryan.taskManager;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestBody;


@Controller // This means that this class is a Controller
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping(path = "/signin")
public class RequestController {

    @Autowired // This means to get the bean called userRepository
               // Which is auto-generated by Spring, we will use it to handle the data
    private UserRepository userRepository;

    @PostMapping(path = "/register") // Map ONLY POST Requests
    public @ResponseBody String addNewUser(@RequestParam String name, @RequestParam String email,
            @RequestParam String password) {
        // @ResponseBody means the returned String is the response, not a view name
        // @RequestParam means it is a parameter from the GET or POST request
        User user = new User();
        try {
            user.setUsername(name);
            user.setEmail(email);
            user.setPassword(password);
            userRepository.save(user);
            return "{'message': 'User " + name + " saved!'}";
        } catch(DataIntegrityViolationException e) {
            return "{'message': 'User already exists!'}";
        }
    }

    @GetMapping(path = "/get/all")
    public @ResponseBody Iterable<User> getAllUsers() {
        // This returns a JSON or XML with the users
        return userRepository.findAll();
    }

    @GetMapping(path = "/get/id")
    public @ResponseBody Optional<User> getUserById(@RequestParam int id) {
        return userRepository.findById(id);
    }

    @GetMapping(path = "/get/email")
    public @ResponseBody Optional<User> getUserByEmail(@RequestParam String email) {
        return userRepository.findByEmail(email);
    }

    @GetMapping(path = "/get/username")
    public @ResponseBody Optional<User> getUserByUsername(@RequestParam String username) {
        return userRepository.findByUsername(username);
    }

    // AUTHENTICATE (LOGIN)
    @CrossOrigin(origins = "http://localhost:5173")
    @GetMapping(path = "/auth/validate")
    public @ResponseBody String validate(@RequestParam String email, @RequestParam String password) {
        try {
            if(userRepository.findByEmailAndPassword(email, password).isPresent()) {
                return "{\"status\": \"true\", \"id\": \"" + userRepository.findByEmailAndPassword(email, password).get().getID() + "\"}";
            }
        } catch(NoSuchElementException e) {
            e.printStackTrace();
        }
        return "{\"status\": \"false\", \"id\": \"" + userRepository.findByEmailAndPassword(email, password).get().getID() + "\"}";
    }

    @GetMapping(path = "/auth/get/token")
    public @ResponseBody String getToken(@RequestParam int ID) {
        User user;
        try {
            user = userRepository.findById(ID).get();
        } catch(NoSuchElementException e) {
            System.out.println("No user described by such ID");
            e.printStackTrace();
            return null;
        }
        Random random = new Random();
        String tok = "";
        String[] characters = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "!", "@", "#", "$", "%", "^", "&", "*", "(", ")", "+", "-", ".", "`", "~", "|", "<", ">", "=", "-", "_"};
        for(int i = 0; i < 255; i++) {
            tok += characters[random.nextInt(characters.length)];
        }
        user.setAccessToken(tok);
        userRepository.flush();
        return "{\"token\": \"" + user.getAccessToken() + "\"}";
    }

    @GetMapping(path = "/auth/check/token")
    public @ResponseBody boolean checkToken(@RequestParam int ID, @RequestParam String accessToken) {
        User user;
        try {
            user = userRepository.findById(ID).get();
        } catch(NoSuchElementException e) {
            System.out.println("No user described by such ID");
            e.printStackTrace();
            return false;
        }

        return user.getAccessToken().equals(accessToken);
    }

    // CREATE WORKSPACE
    @Autowired
    private WorkspaceRepository workspaceRepository;

    @PostMapping(path = "/w/create") // w stands for workspace => when user is actually in workspace, URL will be "/w/{workspace id}"
    public @ResponseBody String createWorkspace(@RequestParam int userID, @RequestParam String workspaceName, @RequestParam boolean isPublic) {
        
        Workspace workspace = new Workspace();
        User user = userRepository.getReferenceById(userID);
        try {
            workspace.setUserID(userRepository.findById(userID).get());
            workspace.setName(workspaceName);
            workspace.setIsPublic(isPublic);
            
            workspaceRepository.save(workspace);
            return "{\"status\": \"success\", \"message\": \"Workspace successfully created!\", \"id\": \"" + workspace.getID() + "\"}";
        } catch(Exception e) {
            return "{\"status\": \"failure\", \"message\": \"Workspace could not be created.\", \"id\": \"" + workspace.getID() + "\"}";
        }

    }

    // CREATE CHART
    @Autowired
    private ChartRepository chartRepository;

    @PostMapping(path = "/c/create") // c stands for chart => when user is actually in chart, URL will be "/c/{chart id}"
    public @ResponseBody String createChart(@RequestParam int workspaceID, @RequestParam String chartName) {
        
        Chart chart = new Chart();
        Workspace workspace = workspaceRepository.getReferenceById(workspaceID);
        try {
            chart.setWorkspaceID(workspaceRepository.findById(workspaceID).get());
            chart.setName(chartName);
            chart.setPosition(chart.getID());
            chartRepository.save(chart);
            return "{\"status\": \"success\", \"message\": \"Chart successfully created!\", \"id\": \"" + chart.getID() + "\"}";
        } catch(Exception e) {
            return "{\"status\": \"failure\", \"message\": \"Chart could not be created.\", \"id\": \"" + chart.getID() + "\"}";
        }

    }

    // CREATE ITEM
    @Autowired
    private ItemRepository itemRepository;

    @PostMapping(path = "/i/create")
    public @ResponseBody String createItem(@RequestParam int chartID, @RequestParam String itemName, @RequestParam String description) {
        
        Item item = new Item();
        Chart chart = chartRepository.getReferenceById(chartID);
        try {
            item.setChartID(chartRepository.findById(chartID).get());
            item.setName(itemName);
            item.setDescription(description);
            item.setPosition(item.getID());
            itemRepository.save(item);
            return "{\"status\": \"success\", \"message\": \"Item successfully created!\", \"id\": \"" + item.getID() + "\"}";
        } catch(Exception e) {
            return "{\"status\": \"failure\", \"message\": \"Item could not be created.\", \"id\": \"" + item.getID() + "\"}";
        }

    }

    

}


