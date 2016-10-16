package siima.util;

public class SwitchCaseTest {
	
	enum GuideView {
	    SEVEN_DAY,
	    NOW_SHOWING,
	    ALL_TIMESLOTS
	}

	public static void main(String[] args) {
		int which =2;
		GuideView whichView = GuideView.values()[which]; //do your own bounds checking
			    switch (whichView) {
			        case SEVEN_DAY:
			           System.out.println("" + which + ":SEVEN_DAY");
			            break;
			        case NOW_SHOWING:
			        	System.out.println("" + which + ":NOW_SHOWING");
			            break;
			        case ALL_TIMESLOTS:
			        	System.out.println("" + which + ":ALL_TIMESLOTS");
			            break;
			        default:
			        	System.out.println("Invalid grade");			        
			        	break;
			    }

	}

}
