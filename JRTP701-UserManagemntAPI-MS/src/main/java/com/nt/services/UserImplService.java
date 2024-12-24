package com.nt.services;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import com.nt.bindings.ActivateUser;
import com.nt.bindings.LoginCredentials;
import com.nt.bindings.RecoverPssword;
import com.nt.bindings.UserAccount;
import com.nt.entity.UserMaster;
import com.nt.repository.IUserMasterRepo;
import com.nt.utils.EmailUtils;

@Service
public class UserImplService implements IUserService 
{
	@Autowired
	private IUserMasterRepo repository;
	@Autowired
	private EmailUtils emailUtils;
	@Autowired
	private Environment env;

	@Override
	public String registeruser(UserAccount ua) throws Exception
	{
		UserMaster master=new UserMaster();
	     BeanUtils.copyProperties(ua,master);
	     String tempPwd= generateRandomPassword(6);
	     master.setPassword(tempPwd);
	     master.setActivate_sw("Inactive");
	     UserMaster saveMaster=repository.save(master);
	     String subject= "User Registration Success";
	     String body=readEmailMessageBody(env.getProperty("mailbody.registeruser.location"),ua.getName(),tempPwd);
	     emailUtils.sendEmailMessage(ua.getEmail(),subject,body);
		return saveMaster!=null?"User with Registered Id Value::"+saveMaster.getUserId()+"check mail for temp password":"Problem is User Registration";
	}//register
	
	@Override
	public String activateuser(ActivateUser au)
	{
		UserMaster entity=repository.findByEmailAndPassword(au.getEmail(), au.getTempPassword());
		if(entity==null)
		{
			return " user is not found for registration";
		}
		else
		{
			entity.setPassword(au.getConfirmPassword());
			entity.setActivate_sw("Active");
			UserMaster updateMaster=repository.save(entity);
			return "user activate with new Password";
			
		}
	}//activate user

	@Override
	public String login(LoginCredentials credentials) {
		UserMaster master=new UserMaster();
		BeanUtils.copyProperties(credentials, master);
		Example<UserMaster> example=Example.of(master);
		List<UserMaster> listEntity=repository.findAll(example);
		if(listEntity.size()==0)
		{
			return "user is not found for activation";
		}
		else {
			UserMaster list=listEntity.get(0);
			if(list.getActivate_sw().equalsIgnoreCase("Active"))
			{
			return " valid Crediantials Login Succeesfully";
			}
		else {
			return "user account is not activate";
		}
		}
	}//login


	@Override
	public List<UserAccount> listuser()
	{
		List<UserAccount> listUser=repository.findAll().stream().map(entity->{
			UserAccount user=new UserAccount();
			BeanUtils.copyProperties(entity,user);
			return user;
		}).toList();	
		return listUser;
	}

	@Override
	public UserAccount UsershowById(Integer id) {
		Optional <UserMaster> opt=repository.findById(id);
		UserAccount account=null;
		if(opt.isPresent())
		{
			account=new UserAccount();
			BeanUtils.copyProperties(opt.get(), account);
		}
		return account;
	}//user show by id

	@Override
	public UserAccount showUserByEmailAndName(String email, String name) {
		UserMaster master=repository.findByNameAndEmail(name, email);
		UserAccount account=null;
		if(master!=null)
		{
			account=new UserAccount();
			BeanUtils.copyProperties(master, account);
			
		}
		return account;
	}

	@Override
	public String updateUser(UserAccount user) {
		Optional<UserMaster> opt=repository.findById(user.getUserId());
		if(opt.isPresent())
		{
			UserMaster master=opt.get();
			BeanUtils.copyProperties(user, master);
			repository.save(master);
			return "User Details updated";
			}
		else {
			return "User Details Not found By Id";
		}
		
	}//update 
	@Override
	public String daleteUser(Integer Id) {
		Optional<UserMaster> opt=repository.findById(Id);
		if(opt.isPresent())
		{
			repository.deleteById(Id);
			return "delete succusfully";
		}
		
	 	return "user id Not found for deleting";
	}

	@Override
	public String changeUserStatus(Integer id, String status) {
		Optional<UserMaster> opt=repository.findById(id);
		if(opt.isPresent())
		{
			UserMaster master=opt.get();
			master.setActivate_sw(status);
		     repository.save(master);
		     return "User Status is Changed";
		}
		return "User Not found for chamging Status";
	}

	@Override
	public String recoverPassword(RecoverPssword recover) throws Exception
	{
		UserMaster master=repository.findByNameAndEmail(recover.getName(),recover.getEmail());
		if(master!=null)
		{
			String pwd=master.getPassword();
			//send the recover password to the mail
			String subject="mail for password recovery";
			String mailBody=readEmailMessageBody(env.getProperty("mailbody.recoverpwd.location"),recover.getName(),pwd);//private method
			emailUtils.sendEmailMessage(recover.getEmail(),subject,mailBody);
			return pwd;
			}
		return "User and Email not found";
	}
	
	
	private String generateRandomPassword(int length) 
	{
		String AlphanumericStr="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		StringBuilder randomWord=new StringBuilder(length);
		int i;
		for(i=0;i<length;i++)
		{
			int ch=(int)(AlphanumericStr.length()*Math.random());
			randomWord.append(AlphanumericStr.charAt(ch));
		}
		return randomWord.toString();
	}//generate password
	
	
	
	
	private String readEmailMessageBody(String fileName,String fullName,String pwd) throws Exception
	{
		String mailBody=null;
		String url="";
		try(FileReader reader=new FileReader(fileName);
				BufferedReader br=new BufferedReader(reader))
		{
			StringBuffer buffer=new StringBuffer();
			String line=null;
			do {
				line=br.readLine();
				buffer.append(line);
				}while(line!=null);
			mailBody=buffer.toString();
			mailBody=mailBody.replace("{FULL-NAME}",fullName);
			mailBody=mailBody.replace("{PWD}",pwd);
			mailBody=mailBody.replace("{URL}",url);
		}//try
		catch(Exception e)
		{
			e.printStackTrace();
			throw e;
		}
		
		return mailBody;
	}
}
