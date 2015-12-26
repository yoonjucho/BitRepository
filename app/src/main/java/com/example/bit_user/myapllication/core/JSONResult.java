package com.example.bit_user.myapllication.core;

import java.sql.Time;
import java.util.ArrayList;

public abstract class JSONResult<DataT> {
	
	private String result;
	private String message;
	private DataT data;
    private DataT data2;
	private Time time;
	private String title;
	private ArrayList<String> votelist;
	private String lessonName;
	
	public JSONResult() {
	}
	
	public JSONResult( String result, String message, DataT data,DataT data2, String lessonName, Time time, String title, ArrayList<String> votelist ) {
		this.result = result;
		this.message = message;
		this.data = data;
        this.data2 = data2;
		this.lessonName = lessonName;
		this.time = time;
		this.title = title;
		this.votelist = votelist;
	}

	public String getLessonName() {
		return lessonName;
	}

	public void setLessonName(String lessonName) {
		this.lessonName = lessonName;
	}

	public Time getTime() {
		return time;
	}

	public void setTime(Time time) {
		this.time = time;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public ArrayList<String> getVotelist() {
		return votelist;
	}

	public void setVotelist(ArrayList<String> votelist) {
		this.votelist = votelist;
	}

	public String getResult() {
		return result;
	}
	
	public void setResult(String result) {
		this.result = result;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isSuccess(){
		return "OK".equals( result );
	}
	
	public DataT getData() { return data; }
	
	public void setData( DataT data ) {
		this.data = data;
	}


    public DataT getData2() { return data2; }

    public void setData2( DataT data2 ) {
        this.data2 = data2;
    }
	
	@Override
	public String toString() {
		return "JSONResult [result=" + result + ", message=" + message + ", data=" + data + "]";
	}	
}