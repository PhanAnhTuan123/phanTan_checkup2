package vn.com.leaseLink.dao;

import org.neo4j.driver.Result;
import org.neo4j.driver.summary.ResultSummary;
import org.neo4j.driver.types.Node;
import vn.com.leaseLink.entity.Doctor;
import vn.com.leaseLink.utils.AppUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DoctorDAOImpl implements DoctorDAO{
    @Override
    public Doctor findDoctorById(String doctorId) {
        String query = "MATCH (d:Doctor {doctor_id: $doctorId}) RETURN d";
        try (var session = AppUtils.getSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query, Map.of("doctorId", doctorId));
                if (result.hasNext()) {
                    var record = result.next();
                    Node node = record.get("d").asNode();
                    return AppUtils.toDoctor(node);
                }
                return null;
            });
        }
    }

    @Override
    public boolean addDoctor(Doctor doctor) {
        try (var session = AppUtils.getSession()) {
            return session.executeWrite(tx -> {
               String query =  "CREATE (d:Doctor {doctor_id: $doctor_id, name: $name, phone: $phone, speciality: $speciality}) RETURN d ";
                ResultSummary summary = tx.run(query, AppUtils.toMap(doctor)).consume();

                return summary.counters().nodesCreated() > 0;
            });
        }
    }

    @Override
    public Map<String, Long> getNoOfDoctorsBySpeciality(String deptName) {
        try (var session = AppUtils.getSession()) {
            return session.executeRead(tx -> {
               String query = "MATCH (d:Doctor) -[:BELONG_TO]-> (dep:Department {name: $deptName})" +
                       " RETURN d.speciality as speciality, count(d) as noOfDoctors";

               Result result = tx.run(query, Map.of("deptName", deptName));
                return result.stream().collect(Collectors.toMap(
                          record -> record.get("speciality").asString(),
                          record -> record.get("noOfDoctors").asLong()
                ));

            });

        }


    }

    @Override
    public List<Doctor> listDoctorsBySpeciality(String keyword) {
        try (var session = AppUtils.getSession()) {
            return session.executeRead(tx -> {
                String query = """
                CALL db.index.fulltext.queryNodes('txt_index_speciality', $keyword)
                YIELD node, score
                RETURN node AS d
            """;

                Result result = tx.run(query, Map.of("keyword", keyword));

                // Trả về danh sách (kể cả nếu rỗng), tránh NullPointerException
                return result.stream()
                        .map(record -> record.get("d").asNode())
                        .map(AppUtils::toDoctor)
                        .collect(Collectors.toList());
            });
        }
    }


    @Override
    public boolean updateDiagnosis(String patientId, String doctorId, String newDiagnosis) {
        try (var session = AppUtils.getSession()) {
            return session.executeWrite(tx -> {
               String query = "MATCH (p:Patient {patient_id: $patientId}) -[r:CONSULT]-> (d:Doctor {doctor_id: $doctorId})" +
                       " SET r.diagnosis = $newDiagnosis RETURN r";
               Map<String, Object> map = Map.of(
                       "patientId",patientId,
                       "doctorId",doctorId,
                       "diagnosis", newDiagnosis
               );
               ResultSummary summary = tx.run(query, map).consume();
               return summary.counters().propertiesSet() > 0;
            });

        }


    }
}
