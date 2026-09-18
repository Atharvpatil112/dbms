package com.school.studentrecords;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class StudentRecordApp {
    private static final AppConfig CONFIG = new AppConfig();
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 8080), 0);
        server.createContext("/", StudentRecordApp::serveFrontend);
        server.createContext("/api/students", StudentRecordApp::studentsApi);
        server.start();
        System.out.println("Student Record System is running at http://localhost:8080");
        System.out.println("Press Ctrl+C to stop the server.");
    }
    private static void serveFrontend(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("GET")) { send(exchange, 405, "text/plain", "Method not allowed"); return; }
        String resource = exchange.getRequestURI().getPath().equals("/") ? "/web/index.html" : "/web" + exchange.getRequestURI().getPath();
        try (InputStream file = StudentRecordApp.class.getResourceAsStream(resource)) {
            if (file == null) { send(exchange, 404, "text/plain", "Not found"); return; }
            send(exchange, 200, resource.endsWith(".css") ? "text/css" : resource.endsWith(".js") ? "application/javascript" : "text/html", new String(file.readAllBytes(), StandardCharsets.UTF_8));
        }
    }
    private static void studentsApi(HttpExchange exchange) throws IOException {
        try {
            Map<String,String> values = query(exchange.getRequestURI().getRawQuery());
            if (exchange.getRequestMethod().equals("POST") || exchange.getRequestMethod().equals("PUT")) values.putAll(query(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8)));
            try (StudentRepository repo = repository(values.getOrDefault("database", "mysql"))) {
                if (exchange.getRequestMethod().equals("GET")) send(exchange, 200, "application/json", toJson(repo.findAll()));
                else if (exchange.getRequestMethod().equals("POST") || exchange.getRequestMethod().equals("PUT")) { Student s=student(values); repo.save(s); send(exchange, 200, "application/json", "{\"message\":\"Student saved\"}"); }
                else if (exchange.getRequestMethod().equals("DELETE")) { String id=exchange.getRequestURI().getPath().replaceFirst(".*/", ""); if(id.equals("students")){send(exchange,400,"application/json","{\"error\":\"Student ID is required\"}");}else send(exchange,200,"application/json","{\"deleted\":"+repo.deleteById(id)+"}"); }
                else send(exchange,405,"application/json","{\"error\":\"Method not allowed\"}");
            }
        } catch (Exception e) { send(exchange, 400, "application/json", "{\"error\":\"" + escape(e.getMessage()) + "\"}"); }
    }
    private static StudentRepository repository(String database) { return database.equals("mongodb") ? new MongoStudentRepository(CONFIG) : new MySqlStudentRepository(CONFIG); }
    private static Student student(Map<String,String> v) { int year=Integer.parseInt(v.getOrDefault("yearLevel","0")); Student s=new Student(v.getOrDefault("studentId","").trim(),v.getOrDefault("fullName","").trim(),v.getOrDefault("email","").trim(),v.getOrDefault("course","").trim(),year,v.getOrDefault("phone","").trim()); if(s.studentId().isBlank()||s.fullName().isBlank()||s.email().isBlank()||s.course().isBlank()||year<1)throw new IllegalArgumentException("Fill all required fields and use a positive year level."); return s; }
    private static Map<String,String> query(String raw) { Map<String,String> m=new HashMap<>(); if(raw==null||raw.isBlank())return m; for(String pair:raw.split("&")){String[] p=pair.split("=",2);m.put(decode(p[0]),p.length>1?decode(p[1]):"");}return m; }
    private static String decode(String s){return URLDecoder.decode(s,StandardCharsets.UTF_8);}
    private static String toJson(List<Student> students){return "["+students.stream().map(s->"{\"studentId\":\""+escape(s.studentId())+"\",\"fullName\":\""+escape(s.fullName())+"\",\"email\":\""+escape(s.email())+"\",\"course\":\""+escape(s.course())+"\",\"yearLevel\":"+s.yearLevel()+",\"phone\":\""+escape(s.phone())+"\"}").reduce((a,b)->a+","+b).orElse("")+"]";}
    private static String escape(String s){return (s==null?"":s).replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n");}
    private static void send(HttpExchange e,int status,String type,String body)throws IOException{byte[] bytes=body.getBytes(StandardCharsets.UTF_8);e.getResponseHeaders().set("Content-Type",type+"; charset=utf-8");e.sendResponseHeaders(status,bytes.length);e.getResponseBody().write(bytes);e.close();}
}

