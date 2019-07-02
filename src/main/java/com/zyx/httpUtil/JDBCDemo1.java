package com.zyx.httpUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;



public class JDBCDemo1 {
	private Connection conn;


	public void initConn(){
		String url="jdbc:mysql://localhost:3306/test";
		String user="root";
		String password="123456";
		
		try {
			
			Class.forName("com.mysql.jdbc.Driver");
			conn=DriverManager.getConnection(url, user, password);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void insert() throws Exception{

		Statement st=conn.createStatement();
		String sql="insert into student(id,name,age)values(3,'nn',28)";
		st.execute(sql);
		System.out.println("over");
		st.close();
		conn.close();
	}

	public void findAll() throws Exception{
		Statement st=conn.createStatement();
		String sql="select * from student";

		ResultSet rs=st.executeQuery(sql);
		int col=rs.getMetaData().getColumnCount();

		while(rs.next()){
			for(int i=1;i<=col;i++){
				// ��String ����ʽ��ȡ�� ResultSet ����ĵ�ǰ����ָ���е�ֵ
				System.out.print(rs.getString(i)+"\t");
			}
			System.out.println();
			//int id=rs.getInt("id");
			//String name=rs.getString("name");
			//int age=rs.getInt("age");
			//System.out.println(id+":"+name+":"+age);
		}
		rs.close();
		st.close();
		conn.close();
	}

}
