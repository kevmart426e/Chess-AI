package cs440_sepia;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.util.Direction;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

public class AStarAgent extends Agent {

 /**
  * A class to represent the location of an object in a map. Stores x, y position as well as the MapLocation
  * that came before it (for path searching), f(n) and g(n) of the node.
  * @author Andrew Wood
  * @author Daniel Kim
  *
  */
    class MapLocation
    {
        public int x, y;
        public MapLocation cameFrom;
        public float g; // dijkstra's path cost: the cost of the path from the src up to the current node
        public float f; // A* estimate vertex cost: the sum of g + h. H is only used here to help A* make
        				// good choices of which path to expand

        public MapLocation(int x, int y, MapLocation cameFrom)
        {
            this.x = x;
            this.y = y;
            this.cameFrom = cameFrom;
            this.g = 0;
            this.f = 0;
        }
        
        public MapLocation(int x, int y, MapLocation cameFrom, float g, float f)
        {
            this.x = x;
            this.y = y;
            this.cameFrom = cameFrom;
            this.g = g;
            this.f = f;
        }
        
        /**
         * for printing purposes.
         */
        @Override
        public String toString() { return "(" + this.x + ", " + this.y + ", g=" + this.g + ", f=" + this.f + ")";}
        
        /**
         * Nodes are equal if they have equivalent x and y positions.
         * @param other the object to compare against.
         * @return true if other is a MapLocation and they have equivalent coordinates.
         */
        @Override
	    public boolean equals(Object other)
        {
        	if(other instanceof MapLocation)
        	{
        		MapLocation b = (MapLocation)other;
        		return this.x == b.x && this.y == b.y;
        	}
        	return false;
	    }
        
        /**
         * A hash function for a MapLocation. This is needed so that nodes can be stored
         * in a HashMap or HashSet. The function is (x^2)*(y^3) because it is a good one to one
         * and onto function.
         * @return the hashed MapLocation.
         */
        @Override
        public int hashCode() { 
         return (int)(this.x * this.x * this.y * this.y * this.y); //avoided Math.pow() for speed.
        }
        
    }
    
    Stack<MapLocation> path;
    int footmanID, townhallID, enemyFootmanID;
    MapLocation nextLoc;

    private long totalPlanTime = 0; // nsecs
    private long totalExecutionTime = 0; //nsecs

    public AStarAgent(int playernum)
    {
        super(playernum);
        this.footmanID = -1;
        this.townhallID = -1;
        this.enemyFootmanID = -1;

        System.out.println("Constructed AstarAgent");
    }

    @Override
    public Map<Integer, Action> initialStep(State.StateView newstate, History.HistoryView statehistory) {
        // get the footman location
        List<Integer> unitIDs = newstate.getUnitIds(playernum);
        
        for(Integer unitID: unitIDs)
        {
        	System.out.println(newstate.getUnit(unitID).getTemplateView().getName());
        }

        if(unitIDs.size() == 0)
        {
            System.err.println("No units found!");
            return null;
        }

        footmanID = unitIDs.get(0);
        
        // double check that this is a footman
        if(!newstate.getUnit(footmanID).getTemplateView().getName().equals("Footman"))
        {
            System.err.println("Footman unit not found");
            return null;
        }

        // find the enemy playernum
        Integer[] playerNums = newstate.getPlayerNumbers();
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
        List<Integer> enemyUnitIDs = newstate.getUnitIds(enemyPlayerNum);

        if(enemyUnitIDs.size() == 0)
        {
            System.err.println("Failed to find enemy units");
            return null;
        }

        townhallID = -1;
        enemyFootmanID = -1;
        for(Integer unitID : enemyUnitIDs)
        {
            Unit.UnitView tempUnit = newstate.getUnit(unitID);
            String unitType = tempUnit.getTemplateView().getName().toLowerCase();
            if(unitType.equals("townhall"))
            {
                townhallID = unitID;
            }
            else if(unitType.equals("footman"))
            {
                enemyFootmanID = unitID;
            }
            else
            {
                System.err.println("Unknown unit type");
            }
        }

        if(townhallID == -1)
        {
            System.err.println("Error: Couldn't find townhall");
            return null;
        }

        long startTime = System.nanoTime();
        path = findPath(newstate);
        totalPlanTime += System.nanoTime() - startTime;

        return middleStep(newstate, statehistory);
    }

    @Override
    public Map<Integer, Action> middleStep(State.StateView newstate, History.HistoryView statehistory)
    {
        long startTime = System.nanoTime();
        long planTime = 0;
        
        Map<Integer, Action> actions = new HashMap<Integer, Action>();

        if(shouldReplanPath(newstate, statehistory, path))
        {
            long planStartTime = System.nanoTime();
            path = findPath(newstate);
            planTime = System.nanoTime() - planStartTime;
            totalPlanTime += planTime;
        }

        Unit.UnitView footmanUnit = newstate.getUnit(footmanID);

        int footmanX = footmanUnit.getXPosition();
        int footmanY = footmanUnit.getYPosition();

        if(!path.empty() && (nextLoc == null || (footmanX == nextLoc.x && footmanY == nextLoc.y)))
        {

            // stat moving to the next step in the path
            nextLoc = path.pop();

            System.out.println("Moving to (" + nextLoc.x + ", " + nextLoc.y + ")");
        }

        if(nextLoc != null && (footmanX != nextLoc.x || footmanY != nextLoc.y))
        {
            int xDiff = nextLoc.x - footmanX;
            int yDiff = nextLoc.y - footmanY;

            // figure out the direction the footman needs to move in
            Direction nextDirection = getNextDirection(xDiff, yDiff);

            actions.put(footmanID, Action.createPrimitiveMove(footmanID, nextDirection));
        } else
        {
            Unit.UnitView townhallUnit = newstate.getUnit(townhallID);

            // if townhall was destroyed on the last turn
            if(townhallUnit == null) {
                terminalStep(newstate, statehistory);
                return actions;
            }

            if(Math.abs(footmanX - townhallUnit.getXPosition()) > 1 ||
                    Math.abs(footmanY - townhallUnit.getYPosition()) > 1)
            {
                System.err.println("Invalid plan. Cannot attack townhall");
                totalExecutionTime += System.nanoTime() - startTime - planTime;
                return actions;
            }
            else {
                System.out.println("Attacking TownHall");
                // if no more movements in the planned path then attack
                actions.put(footmanID, Action.createPrimitiveAttack(footmanID, townhallID));
            }
        }

        totalExecutionTime += System.nanoTime() - startTime - planTime;
        return actions;
    }

    @Override
    public void terminalStep(State.StateView newstate, History.HistoryView statehistory)
    {
        System.out.println("Total turns: " + newstate.getTurnNumber());
        System.out.println("Total planning time: " + totalPlanTime/1e9);
        System.out.println("Total execution time: " + totalExecutionTime/1e9);
        System.out.println("Total time: " + (totalExecutionTime + totalPlanTime)/1e9);
    }

    @Override
    public void savePlayerData(OutputStream os)
    {

    }

    @Override
    public void loadPlayerData(InputStream is)
    {

    }

    /**
     * You will implement this method.
     *
     * This method should return true when the path needs to be replanned
     * and false otherwise. This will be necessary on the dynamic map where the
     * footman will move to block your unit.
     *
     * @param state the state of the environment.
     * @param history the history of the environment
     * @param currentPath the path to check
     * @return
     */
    private boolean shouldReplanPath(State.StateView state, History.HistoryView history, Stack<MapLocation> currentPath)
    {
    	Unit.UnitView enemyFootmanUnit = state.getUnit(enemyFootmanID);
        MapLocation footmanLoc = new MapLocation(enemyFootmanUnit.getXPosition(), enemyFootmanUnit.getYPosition(), null);
        return currentPath.contains(footmanLoc);
    }

    /**
     * This method is implemented for you. You should look at it to see examples of
     * how to find units and resources in Sepia.
     *
     * @param state
     * @return
     */
    private Stack<MapLocation> findPath(State.StateView state)
    {
        Unit.UnitView townhallUnit = state.getUnit(townhallID);
        Unit.UnitView footmanUnit = state.getUnit(footmanID);

        MapLocation startLoc = new MapLocation(footmanUnit.getXPosition(), footmanUnit.getYPosition(), null);

        MapLocation goalLoc = new MapLocation(townhallUnit.getXPosition(), townhallUnit.getYPosition(), null);

        MapLocation footmanLoc = null;
        if(enemyFootmanID != -1)
        {
            Unit.UnitView enemyFootmanUnit = state.getUnit(enemyFootmanID);
            footmanLoc = new MapLocation(enemyFootmanUnit.getXPosition(), enemyFootmanUnit.getYPosition(), null);
        }

        // get resource locations
        List<Integer> resourceIDs = state.getAllResourceIds();
        Set<MapLocation> resourceLocations = new HashSet<MapLocation>();
        for(Integer resourceID : resourceIDs)
        {
            ResourceNode.ResourceView resource = state.getResourceNode(resourceID);

            resourceLocations.add(new MapLocation(resource.getXPosition(), resource.getYPosition(), null));
        }
        
        
        return AstarSearch(startLoc, goalLoc, state.getXExtent(), state.getYExtent(), footmanLoc, resourceLocations);
    }
    /**
     * This is the method you will implement for the assignment. Your implementation
     * will use the A* algorithm to compute the optimum path from the start position to
     * a position adjacent to the goal position.
     *
     * You will return a Stack of positions with the top of the stack being the first space to move to
     * and the bottom of the stack being the last space to move to. If there is no path to the townhall
     * then return null from the method and the agent will print a message and do nothing.
     * The code to execute the plan is provided for you in the middleStep method.
     *
     * As an example consider the following simple map
     *
     * F - - - -
     * x x x - x
     * H - - - -
     *
     * F is the footman
     * H is the townhall
     * x's are occupied spaces
     *
     * xExtent would be 5 for this map with valid X coordinates in the range of [0, 4]
     * x=0 is the left most column and x=4 is the right most column
     *
     * yExtent would be 3 for this map with valid Y coordinates in the range of [0, 2]
     * y=0 is the top most row and y=2 is the bottom most row
     *
     * resourceLocations would be {(0,1), (1,1), (2,1), (4,1)}
     *
     * The path would be
     *
     * (1,0)
     * (2,0)
     * (3,1)
     * (2,2)
     * (1,2)
     *
     * Notice how the initial footman position and the townhall position are not included in the path stack
     *
     * @param start Starting position of the footman
     * @param goal MapLocation of the townhall
     * @param xExtent Width of the map
     * @param yExtent Height of the map
     * @param enemyFootmanLoc the location of the enemy
     * @param resourceLocations Set of positions occupied by resources
     * @return Stack of positions with top of stack being first move in plan
     */
    private Stack<MapLocation> AstarSearch(MapLocation start, MapLocation goal, int xExtent, int yExtent, 
    									   MapLocation enemyFootmanLoc, Set<MapLocation> resourceLocations)
    {
    	PriorityQueue<MapLocation> pq = new PriorityQueue<MapLocation>(new Comparator<MapLocation>()
    	{
    		public int compare(MapLocation a, MapLocation b)
    		{
    			//returns -1 if a < b
    			if (a.f < b.f) {return -1;}
    			// returns +1 If a > b
    			else if (a.f > b.f) {return +1;}
    			// return 0 if a==b
    			return 0;
    		}});
    	Set<MapLocation> finializedVertices = new HashSet<MapLocation>();
    	Map<MapLocation, Float> path2Cost = new HashMap<MapLocation, Float>();
    	pq.add(start);
    	path2Cost.put(start, start.f);
    	while(!pq.isEmpty()) {
    		MapLocation currentMinPath = pq.poll();
    		finializedVertices.add(currentMinPath);
    		if(currentMinPath.equals(goal)) {
    			return this.tracePath(start, currentMinPath.cameFrom, new Stack<MapLocation>());
    		}
    		
    		for(MapLocation childPath: this.getAndCheckNeighbors(currentMinPath.g + 1, currentMinPath, goal, xExtent, 
    				yExtent, enemyFootmanLoc, resourceLocations)) {
    			if(!finializedVertices.contains(childPath)) {
    				pq.add(childPath);
    				path2Cost.put(childPath, childPath.f);
    			} else {
    				Float alternatePathCost = path2Cost.get(childPath);
    				if(alternatePathCost > childPath.f) {
    					path2Cost.replace(childPath, childPath.f);
    					pq.remove(childPath);
    					pq.add(childPath);
    				}
    			}
    		}
    	}
    	return new Stack<MapLocation>();
    }
    
    /**
     * A method to get a Stack of MapLocations representing a path from an initial node to some
     * goal node with neither the initial or goal node appearing in the path. This method operates
     * in linear runtime: O(P) where P is the size of the path.
     * @param start the initial node
     * @param lastNode the node before the goal node on the path.
     * @param path the Stack to fill.
     * @return Stack of positions with the first being the next move to make.
     */
    private Stack<MapLocation> tracePath(MapLocation start, MapLocation lastNode, Stack<MapLocation> path)
    {
    	path.push(lastNode);
     
    	//this works by tracing the path backwards from the lastNode until the parent = start.
    	MapLocation parent = lastNode.cameFrom;
    	while(!parent.equals(start))
    	{
    		path.push(parent);
    		parent = parent.cameFrom;
    	}
    	return path;
    }
    
    /**
     * A method to get a list of MapPositions representing the neighbors of a node that are valid
     * nodes to move to (valid nodes cannot be occupied by other objects such as trees,...).
     * @param parentGPlusEdgeCost The cost g(n) of the parent including the edge to the child we're going to generate
     * @param pt The node to find the neighbors of.
     * @param goal The goal node, used to calculate h(n) from any neighbor.
     * @param xExtent The maximum x boundary of the environment (assuming 0 is min).
     * @param yExtent The maximum y boundary of the environment (assuming 0 is min).
     * @param enemy The location of the enemy in the environment (null if no enemy).
     * @param obstacles A set of obstacles in the environment represented as a set of positions
     * that are already occupied.
     * @return ArrayList of valid positions representing unoccupied neighbors of node pt.
     */
    private ArrayList<MapLocation> getAndCheckNeighbors(float parentGPlusEdgeCost, MapLocation pt, MapLocation goal, int xExtent,
    													int yExtent, MapLocation enemy, Set<MapLocation> obstacles)
    {
     
    	//at max 8 neighbors due to diagonal movement
    	ArrayList<MapLocation> neighbors = new ArrayList<MapLocation>(8);
     
    	//variable to store a neighbor node
    	MapLocation newPt = null;
     
    	if(enemy == null) {
    		for(int i = pt.x - 1; i <= pt.x + 1; i++)
    		{ //run from pt.x-1 -> pt.x+1
    			for(int j = pt.y - 1; j <= pt.y + 1; j++)
    			{ //run from pt.y-1 -> pt.y+1
    				if(i != pt.x || j != pt.y)
    				{ //if we aren't at (pt.x, pt.y)
    					newPt = new MapLocation(i, j, pt, parentGPlusEdgeCost, parentGPlusEdgeCost + this.heuristic(i, j, goal)); //neighbor point
            
    					//if the point is valid (not occupied and in range)
    					if(newPt.x >= 0 && newPt.x < xExtent && newPt.y >= 0 && newPt.y < yExtent
    							&& !obstacles.contains(newPt))
    					{
    						neighbors.add(newPt); //add the point to the neighbors list
    					}
    				}
    			}
    		}
    	} else
    	{
    		for(int i = pt.x - 1; i <= pt.x + 1; i++)
    		{ //run from pt.x-1 -> pt.x+1
    			for(int j = pt.y - 1; j <= pt.y + 1; j++)
    			{ //run from pt.y-1 -> pt.y+1
    				if(i != pt.x || j != pt.y)
    				{ //if we arent at (pt.x, pt.y)
    					newPt = new MapLocation(i, j, pt, parentGPlusEdgeCost, parentGPlusEdgeCost + this.heuristic(i, j, goal)); //neighbor point
         
    					//if the point is valid (not occupied and in range)
    					if(newPt.x >= 0 && newPt.x < xExtent && newPt.y >= 0 && newPt.y < yExtent && 
    							!(newPt.x == enemy.x && newPt.y == enemy.y) && !obstacles.contains(newPt))
    					{
    						neighbors.add(newPt); //add the point to the neighbors list
    					}
    				}
    			}
    		}
    	}
     
    	return neighbors;
    }
    
    /**
     * A method to calculate the heuristic from any position to the goal node. This uses the
     * Chebyshev distance D(x, y) = max(|x0 - x1|, |y0 -y1|).
     * @param x the x position of the node.
     * @param y the y position of the node.
     * @param goal the goal node position.
     * @return the Chebyshev distance from (x, y) -> (goal.x, goal.y)
     */
    private float heuristic(int x,int y, MapLocation goal) { 
    	return Math.max(Math.abs(x - goal.x), Math.abs(y - goal.y));
    }

    /**
     * Primitive actions take a direction (e.g. NORTH, NORTHEAST, etc)
     * This converts the difference between the current position and the
     * desired position to a direction.
     *
     * @param xDiff Integer equal to 1, 0 or -1
     * @param yDiff Integer equal to 1, 0 or -1
     * @return A Direction instance (e.g. SOUTHWEST) or null in the case of error
     */
    private Direction getNextDirection(int xDiff, int yDiff) {

        // figure out the direction the footman needs to move in
        if(xDiff == 1 && yDiff == 1)
        {
            return Direction.SOUTHEAST;
        }
        else if(xDiff == 1 && yDiff == 0)
        {
            return Direction.EAST;
        }
        else if(xDiff == 1 && yDiff == -1)
        {
            return Direction.NORTHEAST;
        }
        else if(xDiff == 0 && yDiff == 1)
        {
            return Direction.SOUTH;
        }
        else if(xDiff == 0 && yDiff == -1)
        {
            return Direction.NORTH;
        }
        else if(xDiff == -1 && yDiff == 1)
        {
            return Direction.SOUTHWEST;
        }
        else if(xDiff == -1 && yDiff == 0)
        {
            return Direction.WEST;
        }
        else if(xDiff == -1 && yDiff == -1)
        {
            return Direction.NORTHWEST;
        }

        System.err.println("Invalid path. Could not determine direction");
        return null;
    }
}
