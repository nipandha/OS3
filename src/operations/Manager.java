package operations;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import data.Task;

public class Manager {

	public int T,R;
	public List<Integer> resource_units,released_units;
	private String input_file;
	public Task[] tasks;
	public String getInput_file() {
		return input_file;
	}
	public void setInput_file(String input_file) {
		this.input_file = input_file;
	}
	public Manager() {
		// TODO Auto-generated constructor stub
		resource_units=new ArrayList<Integer>();
		released_units=new ArrayList<Integer>();
	}
	public void readRequests()
	{
		try {
			Scanner scanner = new Scanner(new File(input_file));
			T=scanner.nextInt();
			R=scanner.nextInt();
			
			tasks=new Task[T+1];
			for(int i=1;i<=T;i++)
			{
				tasks[i]=new Task(i,R);
			}
			
			
			for(int i=0;i<=R;i++)
			{
				resource_units.add(0);
				released_units.add(0);
			}
			for(int i=1;i<=R;i++)
			{
				resource_units.set(i, scanner.nextInt());
			}
			int terminated=0;
			while(terminated!=T)
			{
				String next=scanner.next();
				//System.out.println("Next is: "+next);
				if(next.equals(""))
					continue;
				else if(next.equals("terminate"))
				{
					int t=scanner.nextInt();
					tasks[t].commands.add(next);
					tasks[t].resource_type_or_no_cycles.add(scanner.nextInt());
					tasks[t].number.add(scanner.nextInt());
					terminated++;
				}
				else if((next.equals("request"))||(next.equals("release"))
						||(next.equals("compute")))
				{
					int t=scanner.nextInt();
					tasks[t].commands.add(next);
					tasks[t].resource_type_or_no_cycles.add(scanner.nextInt());
					tasks[t].number.add(scanner.nextInt());
				}
				else if(next.equals("initiate"))
				{
					int t=scanner.nextInt();
					int r=scanner.nextInt();
					int c=scanner.nextInt();
					tasks[t].commands.add(next);
					tasks[t].resource_type_or_no_cycles.add(r);
					tasks[t].number.add(c);
					tasks[t].initial_claims.set(r, c);
				}
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void allocate_bankers()
	{
		int cycle=0,terminated=0;
		List<Integer> blocked_tasks=new ArrayList<Integer>();
		List<Integer> temp_task_index=new ArrayList<Integer>();
		List<Integer> tasks_performed=new ArrayList<Integer>();
		while(terminated!=T)
		{
			
			int i=0;
			for(int x:blocked_tasks)
			{
				Task task=tasks[x];
				int r=task.blocked_resource_type;
				int units=task.blocked_resource_units;
				boolean is_safe_state=true;
				for(int res=1;res<=R;res++)
				{
					if(resource_units.get(res) < (task.initial_claims.get(res) - 
							task.allocated_resources.get(res)))
					{
				
						is_safe_state=false;
					}
				}
				if (!is_safe_state) 
				{
					i++;
					continue;
				}
				else
				{
					int resources = resource_units.get(r) - units;
					resource_units.set(r, resources);
					resources = task.allocated_resources.get(r) + units;
					task.allocated_resources.set(r, resources);
					//System.out.println("Task "+x+"unblocked");
					task.unblock();
					task.finish_instruction();
					temp_task_index.add(i);
					tasks_performed.add(x);
				}
				
				i++;
			}
			for(int x:temp_task_index)
				blocked_tasks.remove(x);
			temp_task_index.clear();
			for(int t=1;t<=T;t++)
			{
				Task task=tasks[t];
				if(task.terminated)
					continue;
				if(task.aborted)
					continue;
				if(task.computing_cycles!=0)
				{
					task.add_computing_cycle();
					if(task.computing_cycles==0)
						task.finish_instruction();
					continue;
				}
				int r = task.resource_type_or_no_cycles.get(task.current_instruction);
				int units = task.number.get(task.current_instruction);
				
				if (task.blocked) 
				{
					task.add_blocked_cycle();
					continue;
				}
				if ((!tasks_performed.contains(t))) {
					String cmd = task.commands.get(task.current_instruction);
					//System.out.println("Command is "+cmd+cycle+t);
					
					if (cmd.equals("initiate"))
					{
						if(task.initial_claims.get(r)>resource_units.get(r))
						{
							task.abort();
							terminated++;
						}
						task.finish_instruction();
						continue;
					}
					if (cmd.equals("terminate")) {
						task.terminate(cycle - 1 + 1);
						task.finish_instruction();
						terminated++;
						continue;
					}
					if (cmd.equals("request")) {
						boolean is_safe_state=true;
						for(int res=1;res<=R;res++)
						{
							if(resource_units.get(res) < (task.initial_claims.get(res) - task.allocated_resources
									.get(res)))
							{
								//System.out.println("Unsafe for task "+t+" at cycle "+cycle+" at resource "+res);
								is_safe_state=false;
							}
						}
						if((task.allocated_resources.get(r)+units)>task.initial_claims.get(r))
						{
							int resources = resource_units.get(r) + task.allocated_resources.get(r);
							resource_units.set(r, resources);
							task.abort();
							terminated++;
						}
						else if (!is_safe_state) {
							//System.out.println("Blocking task "+t);
							blocked_tasks.add(t);
							task.block(r,units);
							task.add_blocked_cycle();
						} 
						else {
							//System.out.println("Task "+t+" requesting "+units);
							int resources = resource_units.get(r) - units;
							resource_units.set(r, resources);
							resources = task.allocated_resources.get(r) + units;
							task.allocated_resources.set(r, resources);
							task.finish_instruction();
						}
					} else if (cmd.equals("release")) {	
						released_units.set(r, units);
						
						int resources = task.allocated_resources.get(r) - units;
						task.allocated_resources.set(r, resources);
						task.finish_instruction();
					} else if (cmd.equals("compute")) {
						
						task.start_computing(r);
						task.add_computing_cycle();
						if(task.computing_cycles==0)
							task.finish_instruction();
					}
				}
				
					
			}
			cycle++;
			for(int j=1;j<=R;j++)
			{
				int resources=resource_units.get(j)+released_units.get(j);
				resource_units.set(j, resources);
			}
			released_units.clear();
			for(int j=0;j<=R;j++)
				released_units.add(0);
			tasks_performed.clear();
		}
	}
	public void print_output()
	{
		System.out.println("\tRun\tWait\tWait%");
		int sum_run=0,sum_wait=0;
		for(int t=1;t<=T;t++)
		{
			Task task=tasks[t];
			if(task.aborted)
			{
				System.out.println("Task "+t+"\taborted");
				continue;
			}
			float per=task.block_time*100/task.end_time;
			sum_run+=task.end_time;
			sum_wait+=task.block_time;
			System.out.println("Task "+t+"\t"+task.end_time+"\t"+task.block_time
					+"\t"+per);
		}
		float per=sum_wait*100/sum_run;
		System.out.println("total\t"+sum_run+"\t"+sum_wait
				+"\t"+per);
	}
	public void allocate_optimistic()
	{
		int cycle=0,terminated=0;
		List<Integer> blocked_tasks=new ArrayList<Integer>();
		List<Integer> temp_task_index=new ArrayList<Integer>();
		List<Integer> tasks_performed=new ArrayList<Integer>();
		while(terminated!=T)
		{
			int i=0;
			for(int x:blocked_tasks)
			{
				Task task=tasks[x];
				//System.out.println("Task "+x+" is blocked");
				int r=task.blocked_resource_type;
				int units=task.blocked_resource_units;
				if(units>resource_units.get(r))
				{
					i++;
					continue;
				}
				else
				{
					int resources = resource_units.get(r) - units;
					resource_units.set(r, resources);
					resources = task.allocated_resources.get(r) + units;
					task.allocated_resources.set(r, resources);
					//System.out.println("Task "+x+"unblocked");
					task.unblock();
					task.finish_instruction();
					temp_task_index.add(i);
					tasks_performed.add(x);
				}
				
				i++;
			}
			//System.out.println(blocked_tasks);
			//System.out.println(temp_task_index);
			for(int x:temp_task_index)
				blocked_tasks.remove(x);
			//System.out.println(blocked_tasks);
			temp_task_index.clear();
			
			for(int t=1;t<=T;t++)
			{
				//System.out.println("On task "+t);
				Task task=tasks[t];
				//System.out.println("Current instruction is "+task.current_instruction);
				if(task.aborted)
				{
					continue;
				}
				if(task.terminated)
				{
					continue;
				}
				if(task.blocked)
				{
					task.add_blocked_cycle();
					continue;
				}
				if(task.computing_cycles!=0)
				{
					task.add_computing_cycle();
					if(task.computing_cycles==0)
						task.finish_instruction();
					continue;
				}
				
				String cmd = task.commands.get(task.current_instruction);
				
				
				
				int r = task.resource_type_or_no_cycles.get(task.current_instruction);
				int units = task.number.get(task.current_instruction);
				if ((!tasks_performed.contains(t)))
				{
					//System.out.println("Command is "+cmd+cycle+t);
					//System.out.println("Not blocked");
					
					
					if(cmd.equals("initiate"))
					{
						task.finish_instruction();
						continue;
					}
					if (cmd.equals("terminate")) 
					{
						task.terminate(cycle - 1 + 1);
						task.finish_instruction();
						terminated++;
						continue;
					}
					if (cmd.equals("request")) 
					{
						if(units>resource_units.get(r)) 
						{
							//System.out.println("Blocking task "+t);
							blocked_tasks.add(t);
							task.block(r,units);
							task.add_blocked_cycle();
						} 
						else 
						{
							//System.out.println("Task "+t+" requesting "+units);
							int resources = resource_units.get(r) - units;
							resource_units.set(r, resources);
							resources = task.allocated_resources.get(r) + units;
							task.allocated_resources.set(r, resources);
							task.finish_instruction();
						}
					}
					else if (cmd.equals("release")) 
					{
						released_units.set(r, units);
						
						int resources = task.allocated_resources.get(r) - units;
						task.allocated_resources.set(r, resources);
						task.finish_instruction();
						
					} 
					else if (cmd.equals("compute")) 
					{
						
						task.start_computing(r);
						task.add_computing_cycle();
						if(task.computing_cycles==0)
							task.finish_instruction();
					}
				}
				
			}
			if((blocked_tasks.size()==(T-terminated))&&(blocked_tasks.size()!=0))
			{

				//System.out.println("All tasks blocked "+blocked_tasks);
				for(int t=1;t<=T;t++)
				{
					Task task=tasks[t];
					if(task.blocked)
					{
						for (int res = 1; res <= R; res++) {
							int resources = resource_units.get(res)
									+ task.allocated_resources.get(res);
							resource_units.set(res, resources);
						}
						//System.out.println(resource_units);
						task.abort();
						terminated++;
						int index=blocked_tasks.indexOf(t);
						if(index!=-1)
						{
							
							blocked_tasks.remove(index);
						}
						
						if ((t+1)<=T) 
						{
							task = tasks[t + 1];
							if (task.blocked_resource_units <= resource_units
									.get(task.blocked_resource_type)) {
								//System.out.println("Deadlock resolved");
								break;
							}
						}
					}
				}
			}
			cycle++;
			
			for(int j=1;j<=R;j++)
			{
				int resources=resource_units.get(j)+released_units.get(j);
				resource_units.set(j, resources);
			}
			released_units.clear();
			for(int j=0;j<=R;j++)
				released_units.add(0);
			tasks_performed.clear();
		}
	}
	public static void main(String[] args)
	{
		Manager a =new Manager();
		a.setInput_file(args[0]);
		a.readRequests();
		a.allocate_optimistic();
		a.print_output();
		Manager b =new Manager();
		b.setInput_file(args[0]);
		b.readRequests();
		b.allocate_bankers();
		b.print_output();	
		
	}
}
