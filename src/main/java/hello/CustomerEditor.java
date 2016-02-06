package hello;

import com.google.gwt.thirdparty.guava.common.collect.Lists;
import com.vaadin.data.Validator;
import com.vaadin.data.validator.NullValidator;
import com.vaadin.ui.*;
import com.zybnet.autocomplete.server.AutocompleteField;
import com.zybnet.autocomplete.server.AutocompleteQueryListener;
import com.zybnet.autocomplete.server.AutocompleteSuggestionPickedListener;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.server.FontAwesome;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.themes.ValoTheme;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * A simple example to introduce building forms. As your real application is
 * probably much more complicated than this example, you could re-use this form in
 * multiple places. This example component is only used in VaadinUI.
 * <p>
 * In a real world application you'll most likely using a common super class for all your
 * forms - less code, better UX. See e.g. AbstractForm in Virin
 * (https://vaadin.com/addon/viritin).
 */
@SpringComponent
@UIScope
public class CustomerEditor extends VerticalLayout {

    private final CustomerRepository repository;

    /**
     * The currently edited customer
     */
    private Customer customer;

    /* Fields to edit properties in Customer entity */
    AutocompleteField<Address> search = new AutocompleteField<>();
    AutocompleteField<Customer> searchCustomer = new AutocompleteField<>();


    TextField firstName = new TextField("Vorname");
    TextField lastName = new TextField("Nachname");
    TextField email = new TextField("Email");
    TextField phone = new TextField("Telefon");
    TextField mobile = new TextField("Mobile");
    DateField birthday = new DateField("Birthday");
    TextField street = new TextField("Strasse");
    TextField streetNr = new TextField("Nr");
    TextField plz = new TextField("PLZ");
    TextField place = new TextField("Ort");

    /* Action buttons */
    Button save = new Button("Save", FontAwesome.SAVE);
    Button cancel = new Button("Cancel");
    Button delete = new Button("Delete", FontAwesome.TRASH_O);
    CssLayout actions = new CssLayout(save, cancel, delete);
    FormLayout form = new FormLayout();

    @Autowired
    public CustomerEditor(CustomerRepository repository) {
        customer = new Customer();
        this.repository = repository;
        search.setWidth("500");
        search.setHeight(50, Unit.PIXELS);
        search.setMinimumQueryCharacters(4);


        search.setQueryListener(new AutocompleteQueryListener<Address>() {
            @Override
            public void handleUserQuery(AutocompleteField<Address> field, String query) {
                for (Address page : queryAddress(query)) {
                    field.addSuggestion(page, page.toString());
                }
            }
        });

        search.setSuggestionPickedListener(this::setAddress);

        searchCustomer.setWidth("500");
        searchCustomer.setHeight(50, Unit.PIXELS);
        searchCustomer.setMinimumQueryCharacters(4);
        searchCustomer.setQueryListener((field, query) -> {
            for (Customer page : queryCustomer(query)) {
                field.addSuggestion(page, page.toString());
            }
        });

        searchCustomer.setSuggestionPickedListener(this::setCustomer);

        firstName.setIcon(FontAwesome.USER);
        firstName.setRequired(true);
        firstName.addValidator(new NullValidator("Pflichtfeld", false));
        lastName.setIcon(FontAwesome.USER);
        lastName.setRequired(true);
        lastName.addValidator(new NullValidator("Pflichtfeld", false));
        street.setIcon(FontAwesome.ROAD);

        email.setIcon(FontAwesome.ENVELOPE);
        phone.setIcon(FontAwesome.PHONE);
        mobile.setIcon(FontAwesome.MOBILE_PHONE);
        birthday.setIcon(FontAwesome.CALENDAR);
        birthday.setDateFormat("yyyy-MM-dd");
        form.addComponents(firstName, lastName, street, streetNr, plz, place, email, phone, mobile, birthday);
        VerticalLayout addressSearch = new VerticalLayout();
        Label addressSearchLabel = new Label("Wo (Ort, Strasse)");
        addressSearch.addComponents(addressSearchLabel, search);

        VerticalLayout customerSearch = new VerticalLayout();
        Label custimerSearchLabel = new Label("Was oder Wen (Telefon, Name)");
        customerSearch.addComponents(custimerSearchLabel, searchCustomer);
        addComponents(addressSearch, customerSearch, form, actions);
        // Configure and style components
        setSpacing(true);
        actions.setStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);
        save.setStyleName(ValoTheme.BUTTON_PRIMARY);
        //save.setClickShortcut(ShortcutAction.KeyCode.ENTER);

        // wire action buttons to save, delete and reset
        save.addClickListener(e -> save());
        delete.addClickListener(e -> repository.delete(customer));
        cancel.addClickListener(e -> editCustomer(customer));
        setVisible(false);
    }

    private void setCustomer(Customer page) {
        firstName.setValue(page.getFirstName());
        lastName.setValue(page.getLastName());
        phone.setValue(page.getPhone());
        streetNr.setValue(page.getNumber());
        place.setValue(page.getPlace());
        plz.setValue(page.getPlz());
        street.setValue(page.getStreet());
    }

    private List<Customer> queryCustomer(String text) {
        List<Customer> addresses = new ArrayList<>();
        RestTemplate restTemplate = new RestTemplate();
        StringBuilder url = new StringBuilder("http://tel.search.ch/api/?key=356884bfb673137a57191b236bd8cdd2&was=" + text);

        ResponseEntity<String> response = restTemplate.getForEntity(
                url.toString(),
                String.class);
        Customer currEmp = null;
        String tagContent = null;
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader reader =
                null;
        try {
            reader = factory.createXMLStreamReader(new ByteArrayInputStream(response.getBody().getBytes("UTF-8")));
            addresses = Lists.newArrayList();

            while (reader.hasNext()) {
                int event = reader.next();

                switch (event) {
                    case XMLStreamConstants.START_ELEMENT:
                        if ("entry".equals(reader.getLocalName())) {
                            currEmp = new Customer();
                            addresses.add(currEmp);
                        }
                        break;

                    case XMLStreamConstants.CHARACTERS:
                        tagContent = reader.getText().trim();
                        break;

                    case XMLStreamConstants.END_ELEMENT:
                        switch (reader.getLocalName()) {
                            case "name":
                                currEmp.setLastName(tagContent);
                                break;
                            case "firstname":
                                currEmp.setFirstName(tagContent);
                                break;
                            case "phone":
                                currEmp.setPhone(tagContent);
                                break;
                            case "streetno":
                                currEmp.setNumber(tagContent);
                                break;
                            case "street":
                                currEmp.setStreet(tagContent);
                                break;
                            case "zip":
                                currEmp.setPlz(tagContent);
                                break;
                            case "city":
                                currEmp.setPlace(tagContent);
                                break;


                        }
                        break;

                }

            }
        } catch (XMLStreamException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        return addresses;

    }

    private Customer save() {

        Customer save = repository.save(customer);
        clear();

        search.focus();
        customer = new Customer();

        return save;
    }

    private void setAddress(Address page) {
        place.setValue(page.getPlace());
        plz.setValue(page.getPlz());
        street.setValue(page.getStreet());


    }

    private void clear() {
        search.clear();
        searchCustomer.clear();
        firstName.setValue("");
        lastName.setValue("");
        email.setValue("");
        phone.setValue("");
        mobile.setValue("");
        birthday.setValue(new Date());
        street.setValue("");
        streetNr.setValue("");
        plz.setValue("");
        place.setValue("");

    }

    private List<Address> queryAddress(String text) {
        List<Address> addresses = new ArrayList<>();
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://tel.search.ch/api/?key=356884bfb673137a57191b236bd8cdd2&wo=" + text,
                String.class);
        Address currEmp = null;
        String tagContent = null;
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader reader =
                null;
        try {
            reader = factory.createXMLStreamReader(new ByteArrayInputStream(response.getBody().getBytes("UTF-8")));
            addresses = Lists.newArrayList();

            while (reader.hasNext()) {
                int event = reader.next();

                switch (event) {
                    case XMLStreamConstants.START_ELEMENT:
                        if ("entry".equals(reader.getLocalName())) {
                            currEmp = new Address();
                            addresses.add(currEmp);
                        }
                        break;

                    case XMLStreamConstants.CHARACTERS:
                        tagContent = reader.getText().trim();
                        break;

                    case XMLStreamConstants.END_ELEMENT:
                        switch (reader.getLocalName()) {
                            case "street":
                                currEmp.setStreet(tagContent);
                                break;
                            case "zip":
                                currEmp.setPlz(tagContent);
                                break;
                            case "city":
                                currEmp.setPlace(tagContent);
                                break;
                            case "streetno":
                                currEmp.setNumber(tagContent);
                                break;


                        }
                        break;

                }

            }
        } catch (XMLStreamException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        return addresses;
    }

    public interface ChangeHandler {

        void onChange();
    }

    public final void editCustomer(Customer c) {
        final boolean persisted = c.getId() != null;
        if (persisted) {
            // Find fresh entity for editing
            customer = repository.findOne(c.getId());
        } else {
            customer = c;
        }
        cancel.setVisible(persisted);

        // Bind customer properties to similarly named fields
        // Could also use annotation or "manual binding" or programmatically
        // moving values from fields to entities before saving
        BeanFieldGroup.bindFieldsUnbuffered(customer, this);

        setVisible(true);

        // A hack to ensure the whole form is visible
        save.focus();
        // Select all text in firstName field automatically
        // firstName.selectAll();
        searchCustomer.focus();
    }

    public void setChangeHandler(ChangeHandler h) {
        // ChangeHandler is notified when either save or delete
        // is clicked
        save.addClickListener(e -> h.onChange());
        delete.addClickListener(e -> h.onChange());

    }

}
