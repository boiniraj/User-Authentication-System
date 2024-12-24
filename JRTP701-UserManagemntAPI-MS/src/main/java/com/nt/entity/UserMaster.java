package com.nt.entity;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name="JRTP701_CASE_WORKS")
@NoArgsConstructor
@AllArgsConstructor
public class UserMaster {
	
	
	@Id
	@SequenceGenerator(name="gen1",sequenceName="App_ID_SEQ",
	initialValue=1000,allocationSize=1)
	@GeneratedValue(generator="gen1",strategy=GenerationType.IDENTITY)
	private Integer userId;
	@Column
	private String name;
	@Column
	private String password;
	@Column()
	private String email;
	@Column
	private String gender;
	@Column
	private BigInteger contactNo;
	@Column
	private BigInteger aadharNo;
	@Column
	private String activate_sw;
	@Column
	private LocalDate dob;
	//metadata
	@CreationTimestamp
	@Column(updatable=false,insertable=true)
	private LocalDate creationTime;
	@UpdateTimestamp
	@Column(updatable=true,insertable=false)
	private LocalDate updateTime;
	@Column
	private String createdBy;
	@Column
	private String updatedBy;
	

}
