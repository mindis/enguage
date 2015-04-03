package com.yagadi.enguage.sofa.tier2;

import com.yagadi.enguage.Enguage;
import com.yagadi.enguage.concept.Tag;
import com.yagadi.enguage.concept.Tags;
import com.yagadi.enguage.expression.Reply;
import com.yagadi.enguage.sofa.Attribute;
import com.yagadi.enguage.sofa.Attributes;
import com.yagadi.enguage.sofa.Value;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Shell;
import com.yagadi.enguage.util.Strings;

public class List extends Value {
	private static       Audit   audit = new Audit( "List" );
	public  static final String   NAME = "list";
	private static       boolean debug = Enguage.runtimeDebugging;
	
	// constructors
	public List( String e, String a ) { super( e, a ); }
	
	// member - List manages a tag which represents 
	private Tag  list = new Tag();
	public  Tags content() { return list.content(); }
	public  Attributes attributes() { return list.attributes(); }
	
	/* ===================================================================================
	 * // Deprecate in v1.3!
	 * These methods are needed to CONVERT any existing iNeed lists into a list tag structure:
	 *  friends
	 *  cookie,2
	 *  coffee,2,cups
	 * INTO
	 * 	<list>
	 * 		<item>friends</item>
	 * 		<item quantity="2">cookie</item>
	 * 		<item quantity="2" unit="cup">coffee</item>
	 * 	<list>
	 * -- quantity not needed, as tuples were experimental...
	 */ /*
	public Tag convertToTags( Tag t ) {
		Strings ss = new Strings( t.prefix(), '\n' );
		for( String s : ss )
			t.content( new Tag().name( "item" ).content( new Tag().prefix( s )));
		t.prefix("");
		return t;
	} // Deprecate in v1.3!
	public boolean isText( Tag t ) { return t.name().equals( "" ); } // Deprecate in v1.3!
	/* ===================================================================================
	 */
	
	
	
	/* NB a) we need to make sure that we don't find an elaborate line with a minor string
	 *  we do need to find minor string if elaborate exists -- to reply i know...
	 */
	private int find( Item item, boolean exact ) { // e.g. ["cake slices","2"]
		if (debug) audit.traceIn( "find", "lookingFor="+ item.toString() +" f/p="+ (exact ? "FULL":"partial"));
		int itemNum = -1;
		for (Tag t : new Tag( getAsString() ).content() ) {  // go though the file
			itemNum++; // note where we are
			if (( exact && t.equals(  item.tag() ))
			 ||	(!exact && t.matchesContent( item.tag() ))) {
				if (debug) audit.traceOut( "FOUND" );
				return itemNum; // found
			}
		}
		if (debug) audit.traceOut( "NOT found" );
		return -1; // not found
	}
	private int count( Item item, boolean exact ) { // e.g. ["cake slices","2"]
		if (debug) audit.traceIn( "count", "Item="+item.toString() + ", exact="+ (exact?"T":"F"));
		int count = 0;
		for (Tag t : new Tag( getAsString() ).content() ) // go though the file
			if (  ( exact && t.equals(  item.tag() ))
				||(!exact && t.matches( item.tag() )))
				count ++;
		if (debug) audit.traceOut( count );
		return count;
	}
	private String quantity( Item item, boolean exact ) { // e.g. ["cake slices","2"]
		if (debug) audit.traceIn( "quantity", "Item="+item.toString() + ", exact="+ (exact?"T":"F"));
		int count = 0;
		for (Tag t : new Tag( getAsString() ).content() ) // go though the file
			if (  ( exact && t.equals( item.tag() ))
				||(!exact && t.matchesContent( item.tag() )))
			{
				int quant = 1;
				try {
					quant = Integer.parseInt( t.attribute( "quantity" ));
				} catch(Exception e) {} // fail silently
				count += quant;
			}
		if (debug) audit.traceOut( Integer.valueOf( count ).toString());
		return Integer.valueOf( count ).toString();
	}
	public Strings get() { return get( null ); }
	public Strings get( Item item ) { // to Items class!
		if (debug) audit.traceIn( "get", "item="+ (item==null?"ALL":item.toString()));
		//Item item = (values == null || values.size() == 0) ? null : new Item( values );
		Strings rc = new Strings();
		String tmp = getAsString();
		Tag list = new Tag( tmp );
		

		// this is only for conversion between v1.1 and v1.2
		// if (isText( list )) list = convertToTags( list );
		
		list.name( "list" );
		for (Tag t : list.content()) 
			if (item == null || t.matches( item.tag()))
				rc.add( new Item( t ).toString());
		
		if (debug) audit.traceOut( rc );
		return rc;
	}
	private String add( Item item ) { // adjusts attributes, e.g. quantity
		if (debug) audit.traceIn( "add", "item created is:"+ item.toXml());
		String rc = Shell.FAIL;
		Tag list = new Tag( getAsString() );
		
		// this is only for conversion between v1.1 and v1.2
		// if (isText( list )) list = convertToTags( list );
		
		list.name( "list" ); // name it just in case we've a blank file.
		
		/* Do we want an exact match? I had set this to exact match???
		 * We need to say "i need a coffee" and "i need a cup of coffee"
		 * SPECIALISATION!
		 * TODO: need an "I also need ..." also situation TO add
		 * coffee and a cup of coffee.
		 */
		int n = find( item, false ); // exact match? No!
		if (-1 == n) {
			// not found so add whole item.
			if (!item.tag().attribute( "quantity" ).equals( "0" )) {
				// in case: quantity='+= 1' => set it to '1'
				Strings quantity = new Strings( item.tag().attribute( "quantity" ));
				if (debug) audit.audit("adding quant:"+ quantity.toString());
				if (quantity.size()==2) {
					item.tag().attributes().remove( "quantity" );
					item.tag().attribute( "quantity", quantity.get( 1 ));
					if (debug) audit.audit( "quant is now:"+ item.tag().attribute( "quantity" ) +":"+ quantity.get( 1 ));
				}
				list.content( item.tag() );
				rc = item.toString();
			}
		} else { // found so update item...
			// does this remove detail too?
			// TODO: need a GENERALISATION just: I just need coffee (from I need a cup of coffee)
			Tag tmp = list.removeContent( n );
			tmp.updateAttributes( item.tag() );
			if (!tmp.attribute( "quantity" ).equals( "0" )) { // only put back if not 0
				tmp.content( item.tag().content() ); // will replace crisp with crisps
				list.content( n, tmp );
				item.tag( tmp );
				rc = item.toString();
		}	}
		
		set( list.toString() ); // was set( lines );
		if (debug) audit.traceOut( rc );
		return rc;
	}
	public boolean remove( Item item, boolean exact ) { // removes an item
		if (debug) audit.traceIn("remove", "item="+ item.toXml() +", exact="+ (exact?"T":"F"));
		Tag list = new Tag( getAsString()); // was get()

		// this is only for conversion between v1.1 and v1.2
		// if (isText( list )) list = convertToTags( list );

		list.name( "list" ); // name it just in case we've a blank file.
		int removed = exact ? list.remove( item.tag() ) : list.removeMatches( item.tag() );
		
		if (removed > 0) set( list.toString() );
		if (debug) audit.traceOut( removed > 0 );
		return removed > 0;
	}
	static private Strings conjunctionFudge( String param ) {
		// e.g. param="ground coffee & carrots & potatoes"
		Strings tmp2 = new Strings(),
				tmp = new Strings( param ); // split the first parameter...
		// now [ "ground", "coffee", "&", "carrots", "&", "potatoes" ]
		
		// compile a new list with the "&"s as separators...
		String tmpStr = "";
		for (String s : tmp ) {
			if ( s.equals( Attribute.VALUE_SEP ) ) {
				if (!tmpStr.equals("")) tmp2.add( new String( tmpStr ));
				tmp2.add( Attribute.VALUE_SEP );
				tmpStr="";
			} else {
				tmpStr += ((tmpStr.equals("") ? "" : " ") + s);
		}	}
		if (!tmpStr.equals("")) tmp2.add( new String( tmpStr ));
		// now: [ "ground coffee", "&", "carrots", "&", "potatoes" ]
		
		// finally, reverse the order (so any name='values' are assoc with the last param)
		return tmp2.reverse(); // now: [ "potatoes", "&", "carrots", "&", "ground coffee" ]
	}
	static public String interpret( Strings sa ) {
		/* What we're doing here is to process the parameters provided in the repertoire
		 * as processed by Intention.class (attributes are expanded) i.e. X ==> x='xvalue'
		 * At this point the "list" is stripped from that conceptualisation.
		 * We need to ensure that the first 5 parameters are re-expanded (so black coffee is
		 * one value/parameter):
		 * (list) get martin needs black coffee quantity='1', or perhaps...
		 * (list) get martin needs  quantity='1' black coffee
		 * This is complicated by the fact that a phrase may have an "and" in it which
		 * means the first (or last) param of each component needs to be converted, and
		 * the operation called for each.
		 */
		String rc = Shell.FAIL;
		if (debug) audit.traceIn( "interpret", sa.toString());
		String cmd = sa.get( 0 );
		
		List list = new List( sa.get( 1 ), sa.get( 2 ));
		
		if (cmd.equals( "delete" )) {
			list.ignore();
			rc = Shell.SUCCESS;
			
		} else if (cmd.equals( "undelete" )) {
			list.restore();
			rc = Shell.SUCCESS;
				
		} else {
			Strings params = sa.copyAfter( 2 );
			if (sa.size() == 3) {
				
				if (cmd.equals("get"))
					rc = list.get().toString( Reply.andListFormat());
				
			} else {
				Strings rca = new Strings();
				if (debug) audit.debug( "params was>"+ params +"<");
				
				/*
				 * At this point "potatoes and carrots" is: [ "potatoes & carrots" ]
				 * whereas "5 pints of beer" is [ "beer", "quantity='5'", "unit='pint'" ]
				 * 
				 * So, split the first param, and append the rest back onto it (now them)
				 */
				params = conjunctionFudge( params.remove( 0 )).append( params );
				/* Then remove ^^^this^^^ code on producing optional parameters.
				 */
				if (debug) audit.debug( "params are:"+ params.toString());
				
				for (Strings itemParams : params.divide( "&" )) {
					Item item = new Item( itemParams );
					if (debug) audit.debug( "item:"+ item.toXml());
					
					if (cmd.equals( "exists" )) {
						/* 
						 * TODO: to "list exists _user needs coffee"
						 * return "FALSE" or "5 cups of coffee" 
						 */
						/* TODO
						 * exists a & b & c + a & b => false (only if all present!)
						 */
						/* Applying an addition of exists:
						 * i.e. a+b+c? with a+b =>false
						 */
						String lastParam = itemParams.get( itemParams.size() - 1 );
						int lineNum = list.find( item, !(lastParam.equals( "quantity='some'" )||lastParam.equals( "quantity='any'" ))); // matches, NOT equals
						if (lineNum != -1) {
							//rca.add( Integer.toString( lineNum ));
							if (rca.size() == 0) rca.add( Shell.SUCCESS );
						} else {
							//rca.add( Integer.toString( lineNum ));
							rca = new Strings();
							rca.add( Shell.FAIL );
							break;
						}
						
					} else if (cmd.equals( "quantity" )) {
						if (debug) audit.audit("itemParams="+ itemParams.toString());
						rca.add( list.quantity( item, false ));
						
					} else if (cmd.equals( "remove" ) || cmd.equals( "removeAny" )) {
						Strings firstParam = new Strings( itemParams.get( 0 ));
						String ref = firstParam.get( 0 ); // first of first
						if (debug) audit.debug( "ref is "+ ref );
						if (Reply.referencers().contains( ref )) { // one or any
							// IN [ "one" "two" "three" ] and [ "one two three", "four fine six" ]
							String newFirstParam = firstParam.copyAfter( 0 ).toString( Strings.SPACED );
							Strings newParams = new Strings();
							newParams.add( newFirstParam );
							newParams.addAll( itemParams.copyAfter( 0 ));
							// OUT [ "two three", "four fine six" ]
							item = new Item( newParams );
							if (debug) audit.audit( "item is now"+ item.toString());
							if (ref.equals( Reply.referencers().get( 0 ) ) // one
								 && (	(1 == list.count( item,  true ) && list.remove( item,  true ))
									  ||(1 == list.count( item, false ) && list.remove( item, false ))
								)	)
								rca.add( Shell.SUCCESS );
							
							else if (!ref.equals( Reply.referencers().get( 0 ) )
									 &&  list.remove( item, false )) // any
								rca.add( Shell.SUCCESS );
							
						} else if (list.remove( item, cmd.equals( "remove" ) ))
							rca.add( Shell.SUCCESS );
						
					} else if (cmd.equals( "add" )) {
						rca.add( list.add( item ));
						
					} else if (cmd.equals("get")) {
						rca.add( list.get( item ).toString( Reply.andListFormat()));
							
				}	}
				// some things (e.g. get) may have many values, some (e.g. exists) only one
				rc = rca.size() == 0 ? Shell.FAIL : rca.toString( Reply.andListFormat());
			}
		}
		if (debug) audit.traceOut( rc );
		return rc;
	}
	// params( "one two = three" ]) => [ "one", "two=three" ]
	static public Strings params( String s ) {
		return new Strings( s ).contract( "+=" ).contract( "-=" ).contract( "=" );
	}
	
	public static void main( String[] argv ) { // sanity check...
		Audit.turnOn();
		Item.format( "QUANTITY,UNIT of,,from FROM" );
		
		String s = List.interpret( params( "get martin needs" ));
		audit.audit( "martin needs:"+ s );
		s = List.interpret( params( "delete martin needs" ));
		audit.audit( "martin needs:"+ s );
		s = List.interpret( params( "add martin needs coffee" ));
		audit.audit( "martin now needs:"+ s );
		s = List.interpret( params( "add martin needs milk" ));
		audit.audit( "martin now needs:"+ s );
		s = List.interpret( params( "add martin needs ground coffee" ));
		audit.audit( "martin now needs:"+ s );
		s = List.interpret( params( "add martin needs coffees quantity='2'" ));
		audit.audit( "quant=2: martin now needs:"+ s );
		s = List.interpret( params( "get martin needs" ));
		audit.audit( "get list: martin needs:"+ s );
		s = List.interpret( params( "removeAny martin needs coffee" ));
		audit.audit( "removed any coffee: martin needs:"+ s );
		s = List.interpret( params( "get martin needs" ));
		audit.audit( "get list: martin needs:"+ s );
		
/*		List.interpret( params( "add martin needs biscuits quantity='2'" ));
		audit.audit( "Start: added 2 biscuits ("+ s +")" );
		
		s = List.interpret( params( "exists martin needs coffee" )); 
		if (s.equals(Shell.FAIL)) {
			audit.audit( "Not coffee found, adding" );
			s = List.interpret( params( "add martin needs quantity=1 unit=cup coffee" ));
			if (s.equals(Shell.FAIL)) {
				audit.ERROR( "Not added!" );
				System.exit( 0 );
			} else {
				audit.audit( "Just check we've added "+ s );
				s = List.interpret( params( "exists martin needs quantity=1 coffee" )); 
		}	}
		audit.audit( "Found coffee in list at #"+ s );
		
		s = List.interpret( params( "get martin needs" ));
		audit.audit( "Full list of Items is\n===>"+ s +"<===" );
		
//		s = List.interpret( params( "exists martin needs coffee" ));
//		audit.audit( "coffee exists at line ===>#"+ s +"<===" );
//		s = List.interpret( params( "update martin needs "+s+" quantity+=1 coffee" ));
//		audit.audit( "Update returns ===>"+ s +"<===" );
		
		s = List.interpret( params( "get martin needs quantity=2 coffee" ));
		audit.audit( "Incr list of Items is ===>"+ s +"<===" );
		
		audit.audit( "Now removing quantity=2 coffee:" );
		List.interpret( params( "remove martin needs quantity=2 coffee" ));
		s = List.interpret( params( "get martin needs" ));
		audit.audit( "Full list of Items w/o coffee is ===>"+ s +"<===" );
		s = List.interpret( params( "exists martin needs quantity=1 coffee" ));
		audit.audit( "Found at "+ s +" (should be "+ Shell.FAIL +")" );

		List.interpret( params( "remove martin needs any biscuits" ));
		audit.audit( "(removed all biscuits)" );
		
		s = List.interpret( params( "get martin needs" ));
		audit.audit( "Full list of Items at end is\n===>"+ s +"<===" );
*/
}	}
