/**
 * FindEventsByCategory managed bean class.
 * @author jperezsalva@uoc.edu
 * @version 1.0
 */
package managedbean;

import java.io.Serializable;
import java.util.*;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.model.SelectItem;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.faces.context.FacesContext;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.faces.component.UIViewRoot;

import jpa.*;
import ejb.EventFacadeRemote;

/**
 * Managed Bean FindEventsByCategory
 */
@ManagedBean(name = "findeventsbycategory")
@SessionScoped
public class FindEventsByCategory implements Serializable {

	private static final long serialVersionUID = 1L;

	@EJB
	private EventFacadeRemote eventFacade;

	private String category;
	protected String keywords;

	protected Collection<SelectItem> eventListItems = new ArrayList<SelectItem>();
	protected Collection<Event> eventList = new ArrayList<Event>();

	protected Event dataEvent;
	protected int idEvent = 0;
	protected int ratting = 0;

	private int screen = 0;
	public boolean disable = true;

	Properties props;

	Context ctx;

	public FindEventsByCategory() throws Exception {

		props = System.getProperties();
		ctx = new InitialContext(props);

		this.eventList();
		this.eventListItems();
	}

	/**
	 * Method get which return Events Collection
	 * 
	 * @return Collection
	 */
	public Collection<SelectItem> getEventListItems() {

		return eventListItems;
	}

	public Collection<Event> getEventList() {

		return eventList;
	}

	public String start(String category) throws Exception {

		String navigation;

		try {

			eventFacade = (EventFacadeRemote) ctx
					.lookup("java:app/eAgenda.jar/EventFacadeBean!ejb.EventFacadeRemote");

		} catch (NamingException e1) {

			return "/errors/internalServerErrorView.xhtml";
		}

		eventList = (Collection<Event>) eventFacade
				.findEventsByCategory(Integer.valueOf(category));
		setEventList(eventList);

		int size = eventList.size();

		switch (size) {

		case 0:

			navigation = "/errors/findEventsErrorView.xhtml";
			break;

		case 1:

			idEvent = Integer.valueOf(eventList.iterator().next().getId()
					.toString());
			setDataEvent(idEvent);

			navigation = "/showFindEventsByCategoryView.xhtml";
			break;

		default:

			navigation = "/listFindEventsByCategoryView.xhtml";
			break;

		}

		return navigation;
	}

	public void setEventList(Collection<Event> eventList) {

		this.eventList = eventList;
	}

	public void eventList() throws Exception {

		eventFacade = (EventFacadeRemote) ctx
				.lookup("java:app/eAgenda.jar/EventFacadeBean!ejb.EventFacadeRemote");

		eventList = (Collection<Event>) eventFacade.listAllEvents();

	}

	public void eventListItems() throws Exception {

		Collection<Event> eventCollection = eventList;

		for (Iterator<Event> iterator = eventCollection.iterator(); iterator
				.hasNext();) {
			Event event = (Event) iterator.next();

			SelectItem item = new SelectItem(event.getId(), event.getName());

			this.eventListItems.add(item);
		}
	}

	public void setDataEvent(int idEvent) throws Exception {

		eventFacade = (EventFacadeRemote) ctx
				.lookup("java:app/eAgenda.jar/EventFacadeBean!ejb.EventFacadeRemote");

		dataEvent = (Event) eventFacade.showEvent(idEvent);

		Set<Keyword> words = dataEvent.getKeyword();

		keywords = "";

		Iterator<Keyword> iterator = words.iterator();

		while (iterator.hasNext())
			keywords += iterator.next().getKeyword() + ", ";

		keywords = (keywords.length() > 0) ? keywords.substring(0,
				keywords.length() - 2)
				+ "." : "";

		ratting = eventFacade.avgRatting(idEvent);
		setRatting(ratting);
	}

	public Event getDataEvent() {

		return dataEvent;
	}

	public String getCategory() {

		return category;
	}

	public void setCategory(String category) {

		this.category = category;
	}

	public String getKeywords() {

		return keywords;
	}

	public void setRatting(int ratting) {

		this.ratting = ratting;

	}

	public int getRatting() {

		return ratting;
	}

	public void validate(FacesContext context, UIComponent component,
			Object value) throws ValidatorException {

		boolean isANumber = true;

		if (((String) value).length() < 1)
			throw new ValidatorException(new FacesMessage(
					"You must enter a Category."));

		if (((String) value).length() > 9)
			throw new ValidatorException(new FacesMessage(
					"Please, enter a valid value."));

		for (int i = 0; i < ((String) value).length(); i++) {

			if (!Character.isDigit(((String) value).charAt(i)))
				isANumber = false;
		}

		if (isANumber == false)
			throw new ValidatorException(new FacesMessage(
					"It's not an acceptable value."));

		if (Integer.valueOf((String) value) < 1) {
			throw new ValidatorException(new FacesMessage(
					"Category is shorter than allowable minimum."));
		}

	}

	public boolean getDisable() {

		if (eventList.size() == 0) {
			disable = true;

		} else {
			disable = false;

		}

		return disable;
	}

	/**
	 * allows forward or backward in user screens
	 */
	public void nextScreen() {

		if (((screen + 1) * 10 < eventList.size())) {
			screen += 1;
		}
	}

	public void previousScreen() {

		if ((screen > 0)) {
			screen -= 1;
		}
	}
}