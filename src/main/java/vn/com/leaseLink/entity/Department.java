package vn.com.leaseLink.entity;

import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Setter
@Getter
public class Department implements Serializable {
    private String id;
    private String name;
    private String location;


}
