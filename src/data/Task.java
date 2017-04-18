package data;

import java.util.ArrayList;
import java.util.List;

public class Task {

	public boolean terminated,blocked,aborted;
	public List<Integer> initial_claims,resource_type_or_no_cycles,number,allocated_resources;
	public List<String> commands;
	public int block_time,current_instruction,end_time,computing_cycles,blocked_resource_units,blocked_resource_type;
	int id;
	public Task(int id, int n_r) {
		// TODO Auto-generated constructor stub
		this.id=id;
		current_instruction=0;
		aborted=false;
		terminated=false;
		blocked=false;
		block_time=0;
		end_time=-1;
		computing_cycles=0;
		initial_claims=new ArrayList<Integer>();
		allocated_resources=new ArrayList<Integer>();
		resource_type_or_no_cycles=new ArrayList<Integer>();
		number=new ArrayList<Integer>();
		commands=new ArrayList<String>();
		for(int i=0;i<=n_r;i++)
			initial_claims.add(0);
		for(int i=0;i<=n_r;i++)
			allocated_resources.add(0);
	}
	public void terminate(int time)
	{
		terminated=true;
		end_time=time;
	}
	public void finish_instruction()
	{
		current_instruction++;
	}
	public void abort()
	{
		aborted=true;
	}
	public void block(int x,int y)
	{
		blocked_resource_type=x;
		blocked_resource_units=y;
		blocked=true;
	}
	public void unblock()
	{
		blocked=false;
	}
	public void add_blocked_cycle()
	{
		block_time++;
	}
	public void start_computing(int x)
	{
		computing_cycles=x;
	}
	public void add_computing_cycle()
	{
		computing_cycles--;
	}
}
