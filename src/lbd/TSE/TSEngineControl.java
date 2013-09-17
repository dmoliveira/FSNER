package lbd.TSE;

import java.util.ArrayList;

import lbd.TSE.TSEngine.State;

public class TSEngineControl {
	
	protected ArrayList<TSEngine> tSEngineList;
	protected int maxTSEnginePerTime;
	
	public TSEngineControl(int maxTSEnginePerTime) {
		this.maxTSEnginePerTime = maxTSEnginePerTime;
		tSEngineList = new ArrayList<TSEngine>();
	}
	
	public void add(TSEngine tsengine) {
		tSEngineList.add(tsengine);
	}
	
	public void waitUntilCanAddMore() {
		while(!canAddMore());
		
		//-- wait to not be banned by twitter
		wait(1000);
	}
	
	public boolean canAddMore() {
		
		ArrayList<TSEngine> markToRemove = new ArrayList<TSEngine>();
		
		if(tSEngineList.size() == maxTSEnginePerTime) {
			
			for(TSEngine engine : tSEngineList) {
				if(engine.getState() == State.Waiting) {
					markToRemove.add(engine);
				}
			}
			
			for(TSEngine engine : markToRemove) {
				tSEngineList.remove(engine);
			}
		}
		
		return(tSEngineList.size() < maxTSEnginePerTime);
	}
	
	public void waitUntilAllFinish() {
		
		ArrayList<TSEngine> markToRemove = new ArrayList<TSEngine>();
		
		while(tSEngineList.size() != 0) {
			
			for(TSEngine engine : tSEngineList) {
				if(engine.getState() == State.Waiting) {
					markToRemove.add(engine);
				}
			}
			
			for(TSEngine engine : markToRemove) {
				tSEngineList.remove(engine);
			}
			
			markToRemove.clear();
		}		
	}
	
	public static void wait(int miliseconds) {
		try {
			Thread.sleep(miliseconds); // 1s
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public TSEngine getLastTSEngine() {
		return(tSEngineList.get(tSEngineList.size() - 1));
	}

}
