package vn.com.leaseLink;

import lombok.SneakyThrows;
import vn.com.leaseLink.dao.DoctorDAO;
import vn.com.leaseLink.dao.DoctorDAOImpl;
import vn.com.leaseLink.entity.Doctor;

import javax.print.Doc;
import java.io.DataInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Server {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(9090)) {
            System.out.println("Server is ready!!!");
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println(socket.getInetAddress());
                System.out.println(socket.getPort());

                Thread thread = new Thread(new HandlingClient(socket));
                thread.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
class HandlingClient implements Runnable {
    private Socket socket;
    private DoctorDAO doctorDAO;

    public HandlingClient(Socket socket) {
        this.socket = socket;
        this.doctorDAO = new DoctorDAOImpl();
    }

    @Override
    public void run() {
        try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String command = in.readUTF();

                switch (command) {
                    case "FIND_DOCTOR" -> {
                        String doctorId = in.readUTF();

                        Doctor doctor = doctorDAO.findDoctorById(doctorId);
                        out.writeObject(doctor);
                        out.flush();
                    }

                    case "ADD_DOCTOR" -> {
                        String id = in.readUTF();
                        String name = in.readUTF();
                        String speciality = in.readUTF();
                        String phone = in.readUTF();
                        Doctor doctor = new Doctor(id, name, phone, speciality);
                        boolean result = doctorDAO.addDoctor(doctor);
                        out.writeBoolean(result);
                        out.flush();
                    }

                    case "GET_DOCTOR_COUNT" -> {
                        System.out.print("Start get doctor ");
                        String deptName = in.readUTF();
                        Map<String, Long> doctorCount = doctorDAO.getNoOfDoctorsBySpeciality(deptName);
                        out.writeObject(doctorCount);
                        out.flush();
                    }

                    case "UPDATE_DIAGNOSIS" -> {
                        String patientId = in.readUTF();
                        String doctorId = in.readUTF();
                        String newDiagnosis = in.readUTF();
                        boolean result = doctorDAO.updateDiagnosis(patientId, doctorId, newDiagnosis);
                        out.writeBoolean(result);
                        out.flush();
                    }
                    case "LIST_DOCTORS" -> {
                        String keywords = in.readUTF();
                        System.out.println(keywords);

                        List<Doctor> doctors = doctorDAO.listDoctorsBySpeciality(keywords);
//                      System.out.println(doctors);
                        out.writeObject(doctors);
                        out.flush();
                    }

                    case "EXIT" -> {
                        System.out.println("Client disconnected!");
                        socket.close();
                        return;
                    }

                    default -> System.out.println("Invalid command!");
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }



    }
}