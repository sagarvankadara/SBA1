package com.iiht.evaluation.eloan.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;

import com.iiht.evaluation.eloan.dto.LoanDto;
import com.iiht.evaluation.eloan.model.ApprovedLoan;
import com.iiht.evaluation.eloan.model.LoanInfo;
import com.iiht.evaluation.eloan.model.User;
import com.mysql.cj.protocol.Resultset;

public class ConnectionDao 
{
	private static final long serialVersionUID = 1L;
	private String jdbcURL;
	private String jdbcUsername;
	private String jdbcPassword;
	private Connection jdbcConnection;

	public ConnectionDao(String jdbcURL, String jdbcUsername, String jdbcPassword) 
	{
		this.jdbcURL = jdbcURL;
		this.jdbcUsername = jdbcUsername;
		this.jdbcPassword = jdbcPassword;
	}

	public  Connection connect() throws SQLException 
	{
		if (jdbcConnection == null || jdbcConnection.isClosed()) 
		{
			try 
			{
				Class.forName("com.mysql.jdbc.Driver");
			}
			catch (ClassNotFoundException e) 
			{
				throw new SQLException(e);
			}
			jdbcConnection = DriverManager.getConnection(jdbcURL, jdbcUsername, jdbcPassword);
		}
		return jdbcConnection;
	}

	public void disconnect() throws SQLException 
	{
		if (jdbcConnection != null && !jdbcConnection.isClosed()) 
		{
			jdbcConnection.close();
		}
	}

	public String validateUser(User user) throws SQLException 
	{
		String Page_Look = null;
		this.connect();

		try 
		{
			String sqlquery = "select * from user where username=? and password=?";

			PreparedStatement pst = connect().prepareStatement(sqlquery);
			pst.setString(1, user.getUsername());
			pst.setString(2, user.getPassword());

			ResultSet resultset = pst.executeQuery();

			if (resultset.next()) 
			{
				if (user.getUsername().equals("admin")) 
				{
					Page_Look = "adminhome1.jsp";
				} 
				else 
				{
					Page_Look = "userhome1.jsp";
				}
			} 
			else 
			{
				Page_Look = "index.jsp";
			}
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}

		this.disconnect();

		return Page_Look;
	}

	public void registerNewUser(User user) throws SQLException 
	{
		this.connect();

		String sqlquery = "insert into user (username,password) values(?,?)";

		PreparedStatement preparestate = connect().prepareStatement(sqlquery);
		preparestate.setString(1, user.getUsername());
		preparestate.setString(2, user.getPassword());

		preparestate.executeUpdate();

		this.disconnect();
	}

	public void newLoanApplication(LoanInfo loanInfo) throws SQLException 
	{
		this.connect();

		String sqlquery = "insert into loaninfo(purpose, amtrequest, doa, bstructure, bindicator, tindicator, address, email, mobile, status, username) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		PreparedStatement preparestate = connect().prepareStatement(sqlquery);
		preparestate.setString(1, loanInfo.getPurpose()); 
		preparestate.setInt(2, loanInfo.getAmtrequest()); 
		preparestate.setString(3, loanInfo.getDoa());
		preparestate.setString(4, loanInfo.getBstructure()); 
		preparestate.setString(5, loanInfo.getBindicator());
		preparestate.setString(6, loanInfo.getTindicator());
		preparestate.setString(7, loanInfo.getAddress()); 
		preparestate.setString(8, loanInfo.getEmail()); 
		preparestate.setString(9, loanInfo.getMobile());
		preparestate.setString(10, loanInfo.getStatus()); 
		preparestate.setString(11, loanInfo.getUsername());
		int results = preparestate.executeUpdate(); 

		this.disconnect();
	}

	public List<LoanInfo> getLoanDetails(String applno, String username) throws SQLException 
	{
		this.connect();
		
		String sqlquery = "select * from loaninfo where applno=? and username=?";
		
		PreparedStatement preparestate = connect().prepareStatement(sqlquery);
		
		preparestate.setString(1, applno);
		preparestate.setString(2, username);
		
		ResultSet resultset = preparestate.executeQuery();
		
		List<LoanInfo> loans = new ArrayList<LoanInfo>();
		
		while(resultset.next())
		{
			LoanInfo loaninfo = new LoanInfo(resultset.getString(1), resultset.getString(2), resultset.getInt(3), resultset.getString(4), resultset.getString(5), resultset.getString(6), resultset.getString(7), resultset.getString(8), resultset.getString(9), resultset.getString(10), resultset.getString(11), resultset.getString(12));
			loans.add(loaninfo);
		}		
		
		this.disconnect();
		return loans;
	}

	public void editLoanDetails(LoanInfo loanInfo) throws SQLException 
	{
		this.connect();
		
		String status_query = "select status from loaninfo where applno=?";
		PreparedStatement statement = jdbcConnection.prepareStatement(status_query);
		statement.setString(1, loanInfo.getApplno());
		ResultSet resultSet = statement.executeQuery();

		if(resultSet.next())
		{
			if(resultSet.getString(1).equalsIgnoreCase("rejected") || resultSet.getString(1).equalsIgnoreCase("approved"))
			{
				System.out.println("No permission to edit");
			}
			else if(resultSet.getString(1).equalsIgnoreCase("new") || resultSet.getString(1).equalsIgnoreCase("pending") )
			{
				String sqlquery = "update loaninfo set amtrequest=?, bstructure=?, bindicator=?, tindicator=?, address=?, email=?, mobile=? where applno=?";
				
				PreparedStatement preparestate = connect().prepareStatement(sqlquery);
				preparestate.setInt(1, loanInfo.getAmtrequest());
				preparestate.setString(2, loanInfo.getBstructure());
				preparestate.setString(3, loanInfo.getBindicator());
				preparestate.setString(4, loanInfo.getTindicator());
				preparestate.setString(5, loanInfo.getAddress());
				preparestate.setString(6, loanInfo.getEmail());
				preparestate.setString(7, loanInfo.getMobile());
				preparestate.setString(8, loanInfo.getApplno());
				
				preparestate.executeUpdate();
			}
		}
		
		this.disconnect();
	}

	public void changeStatus(int applno, String loanStatus) throws SQLException 
	{
		this.connect();
		
		String sqlquery = "update loaninfo set status=? where applno=?;";
		
		PreparedStatement preparestate = connect().prepareStatement(sqlquery);
		
		preparestate.setString(1, loanStatus);
		preparestate.setInt(2, applno);
		
		preparestate.executeUpdate();
		
		this.disconnect();
	}

	public LoanDto calEmi(ApprovedLoan approvedloan) throws SQLException 
	{
		int interest_Rate = 7;
		
		this.connect();
		
		String sqlquery = "select amtrequest from loaninfo where applno=?;";
		
		PreparedStatement preparestate = connect().prepareStatement(sqlquery);
		preparestate.setInt(1, Integer.parseInt(approvedloan.getApplno()));
	
		ResultSet resultset = preparestate.executeQuery();
		resultset.next();
		
		int sanctioned_Loan_Amount = resultset.getInt(1);
		int loan_Term = approvedloan.getLoanterm();
		int term_Payment_Amount = (sanctioned_Loan_Amount) * (1 + interest_Rate / 100) ^ (loan_Term);
		int emi = term_Payment_Amount / loan_Term;
		
		approvedloan.setEmi(emi);
		approvedloan.setAmotsanctioned(resultset.getInt(1));
		
		LoanDto loanDto = new LoanDto(approvedloan.getApplno(), approvedloan.getAmotsanctioned(), emi);
		return loanDto;		
	}

	public List<ApprovedLoan> updateEMIDetails(ApprovedLoan apprloan) throws SQLException 
	{
		List<ApprovedLoan> approvedLoansList = new ArrayList<ApprovedLoan>();
		
		String sqlCommand = "insert into approvedloan values(?, ?, ?, ?, ?, ?)";
		
		PreparedStatement preparestate = connect().prepareStatement(sqlCommand);
		preparestate.setString(1, apprloan.getApplno());
		preparestate.setInt(2, apprloan.getAmotsanctioned());
		preparestate.setInt(3, apprloan.getLoanterm());
		preparestate.setString(4, apprloan.getPsd());
		preparestate.setString(5, apprloan.getLcd());
		preparestate.setInt(6, apprloan.getEmi());
		preparestate.executeUpdate();
		
		String sqlCommandForDisplay = "select distinct * from approvedloan WHERE applno='" + apprloan.getApplno()+ "';";
		
		Statement st = connect().createStatement();
		ResultSet resultset = st.executeQuery(sqlCommandForDisplay);
		while (resultset.next()) 
		{
			String applno = resultset.getString(1);
			int amountsanctioned = resultset.getInt(2);
			int loanterm = resultset.getInt(3);
			String psd = resultset.getString(4);
			String lcd = resultset.getString(5);
			int emi = resultset.getInt(6);
			ApprovedLoan approvedLoan = new ApprovedLoan(applno, amountsanctioned, loanterm, psd, lcd, emi);
			approvedLoansList.add(approvedLoan);
		}
		return approvedLoansList;
	}
}