package vn.com.leaseLink.utils;

import org.neo4j.driver.*;
import org.neo4j.driver.types.Node;
import vn.com.leaseLink.entity.Doctor;

import java.util.Map;

public class AppUtils {

    public static final String DB_NAME = "neo4j";


    public static Driver getDriver() {
        String username = "neo4j";
        String password = "your_password";
        String uri = "neo4j://localhost:7687";
        return GraphDatabase.driver(uri, AuthTokens.basic(username, password));
    }

    public static Session getSession() {
        return getDriver().session(SessionConfig.forDatabase(DB_NAME));
    }

    public static Doctor toDoctor(Node node) {
        return new Doctor(
                node.get("doctor_id").asString(),
                node.get("name").asString(),
                node.get("phone").asString(),
                node.get("speciality").asString()
        );
    }

    public static Map<String, Object> toMap(Doctor doctor) {
        return Map.of(
                "doctor_id", doctor.getId(),
                "name", doctor.getName(),
                "phone", doctor.getPhone(),
                "speciality", doctor.getSpeciality()
        );
    }


}
