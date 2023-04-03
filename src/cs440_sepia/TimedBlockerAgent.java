package cs440_sepia;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.history.History.HistoryView;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.environment.model.state.State.StateView;

public class TimedBlockerAgent extends Agent
{

	boolean blockAttempt1, blockAttempt2;
	
	public TimedBlockerAgent(int playerNum)
	{
		super(playerNum);
		this.blockAttempt1 = false;
		this.blockAttempt2 = false;
	}

	public boolean getBlockAttempt1()
	{
		return this.blockAttempt1;
	}
	
	public boolean getBlockAttempt2()
	{
		return this.blockAttempt2;
	}
	
	public void setBlockAttempt1(boolean b)
	{
		this.blockAttempt1 = b;
	}
	
	public void setBlockAttempt2(boolean b)
	{
		this.blockAttempt2 = b;
	}
	
	@Override
	public Map<Integer, Action> initialStep(StateView arg0, HistoryView arg1) {
		Map<Integer, Action> actions = new HashMap<Integer, Action>();
		return actions;
	}

	@Override
	public void loadPlayerData(InputStream arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public Map<Integer, Action> middleStep(StateView stateView, HistoryView historyView)
	{
		Map<Integer, Action> actions = new HashMap<Integer, Action>();

		List<Integer> unitIDs = stateView.getUnitIds(this.getPlayerNumber());
        if(unitIDs.size() == 0)
        {
            System.err.println("No units found!");
            return null;
        }

        boolean townHallAlive = false;
        Integer myFootManID = -1;
        for(Integer unitID : unitIDs)
        {
        	if(stateView.getUnit(unitID).getTemplateView().getName().equals("TownHall"))
        	{
        		townHallAlive = true;
        	} else if(stateView.getUnit(unitID).getTemplateView().getName().toLowerCase().equals("footman"))
        	{
        		myFootManID = unitID;
        	}
        }
        // System.out.println("TimedBlockerAgent: " + " " + townHallAlive);

        if(!townHallAlive)
        {
        	actions.put(myFootManID, Action.createPrimitiveAttack(myFootManID, myFootManID));
        } else
        {
        	Integer[] playerNums = stateView.getPlayerNumbers();
            int enemyPlayerNum = -1;
            for(Integer playerNum : playerNums)
            {
                if(playerNum != playernum) {
                    enemyPlayerNum = playerNum;
                    break;
                }
            }

            if(enemyPlayerNum == -1)
            {
                System.err.println("Failed to get enemy player number");
                return null;
            }
            
         // find the townhall ID
            List<Integer> enemyUnitIDs = stateView.getUnitIds(enemyPlayerNum);

            if(enemyUnitIDs.size() == 0)
            {
                System.err.println("Failed to find enemy units");
                return null;
            }

            Integer enemyFootmanID = -1;
            for(Integer unitID : enemyUnitIDs)
            {
                Unit.UnitView tempUnit = stateView.getUnit(unitID);
                String unitType = tempUnit.getTemplateView().getName().toLowerCase();
                if(unitType.equals("footman"))
                {
                    enemyFootmanID = unitID;
                }
                else
                {
                    System.err.println("Unknown unit type");
                }
            }
            
            Unit.UnitView footmanUnit = stateView.getUnit(enemyFootmanID);
            int xPosition = footmanUnit.getXPosition();
            int yPosition = footmanUnit.getYPosition();
            if(xPosition == 11 && yPosition == 19 && !this.getBlockAttempt1())
            {
            	this.setBlockAttempt1(true);
            	actions.put(myFootManID, Action.createCompoundMove(myFootManID, 25, 13));
            } else if(this.getBlockAttempt1() && xPosition == 6 && yPosition == 15 && !this.getBlockAttempt2())
            {
            	this.setBlockAttempt2(true);
            	actions.put(myFootManID, Action.createCompoundMove(myFootManID, 17, 7));
            }
        }
		
		return actions;
	}

	@Override
	public void savePlayerData(OutputStream arg0)
	{
	}

	@Override
	public void terminalStep(StateView stateView, HistoryView historyView)
	{
	}

}
