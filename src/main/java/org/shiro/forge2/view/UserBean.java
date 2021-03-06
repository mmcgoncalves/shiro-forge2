package org.shiro.forge2.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.shiro.forge2.model.User;

/**
 * Backing bean for User entities.
 * <p/>
 * This class provides CRUD functionality for all User entities. It focuses
 * purely on Java EE 6 standards (e.g. <tt>&#64;ConversationScoped</tt> for
 * state management, <tt>PersistenceContext</tt> for persistence,
 * <tt>CriteriaBuilder</tt> for searches) rather than introducing a CRUD framework or
 * custom base class.
 */

@Named
@Stateful
@ConversationScoped
public class UserBean implements Serializable
{

   private static final long serialVersionUID = 1L;

   /*
    * Support creating and retrieving User entities
    */

   private Integer id;

   public Integer getId()
   {
      return this.id;
   }

   public void setId(Integer id)
   {
      this.id = id;
   }

   private User user;

   public User getUser()
   {
      return this.user;
   }

   public void setUser(User user)
   {
      this.user = user;
   }

   @Inject
   private Conversation conversation;

   @PersistenceContext(unitName = "shiro-forge2-persistence-unit", type = PersistenceContextType.EXTENDED)
   private EntityManager entityManager;

   public String create()
   {
      this.conversation.begin();
      this.conversation.setTimeout(1800000L);
      return "create?faces-redirect=true";
   }
   
   private void generatePassword(User user, String plainTextPassword) {
		RandomNumberGenerator rng = new SecureRandomNumberGenerator();
		Object salt = rng.nextBytes();
	 
		// Now hash the plain-text password with the random salt and multiple
		// iterations and then Base64-encode the value (requires less space than Hex):
		String hashedPasswordBase64 = new Sha256Hash(plainTextPassword, salt,1024).toBase64();
	 
		user.setPassword(hashedPasswordBase64);
		user.setSalt(salt.toString());
	}

   public void retrieve()
   {

      if (FacesContext.getCurrentInstance().isPostback())
      {
         return;
      }

      if (this.conversation.isTransient())
      {
         this.conversation.begin();
         this.conversation.setTimeout(1800000L);
      }

      if (this.id == null)
      {
         this.user = this.example;
      }
      else
      {
         this.user = findById(getId());
      }
   }

   public User findById(Integer id)
   {

      return this.entityManager.find(User.class, id);
   }

   /*
    * Support updating and deleting User entities
    */

   public String update()
   {
      this.conversation.end();

      try
      {
         if (this.id == null)
         {         
     		RandomNumberGenerator rng = new SecureRandomNumberGenerator();
    		Object salt = rng.nextBytes();    	 
    		// Now hash the plain-text password with the random salt and multiple
    		// iterations and then Base64-encode the value (requires less space than Hex):
    		String hashedPasswordBase64 = new Sha256Hash(this.user.getPassword(), salt,1024).toBase64();    	 
    		this.user.setPassword(hashedPasswordBase64);
    		this.user.setSalt(salt.toString());
    		
            this.entityManager.persist(this.user);
            return "search?faces-redirect=true";
         }
         else
         {
            this.entityManager.merge(this.user);
            return "view?faces-redirect=true&id=" + this.user.getId();
         }
      }
      catch (Exception e)
      {
         FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(e.getMessage()));
         return null;
      }
   }

   public String delete()
   {
      this.conversation.end();

      try
      {
         User deletableEntity = findById(getId());

         this.entityManager.remove(deletableEntity);
         this.entityManager.flush();
         return "search?faces-redirect=true";
      }
      catch (Exception e)
      {
         FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(e.getMessage()));
         return null;
      }
   }

   /*
    * Support searching User entities with pagination
    */

   private int page;
   private long count;
   private List<User> pageItems;

   private User example = new User();

   public int getPage()
   {
      return this.page;
   }

   public void setPage(int page)
   {
      this.page = page;
   }

   public int getPageSize()
   {
      return 10;
   }

   public User getExample()
   {
      return this.example;
   }

   public void setExample(User example)
   {
      this.example = example;
   }

   public String search()
   {
      this.page = 0;
      return null;
   }

   public void paginate()
   {

      CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();

      // Populate this.count

      CriteriaQuery<Long> countCriteria = builder.createQuery(Long.class);
      Root<User> root = countCriteria.from(User.class);
      countCriteria = countCriteria.select(builder.count(root)).where(
            getSearchPredicates(root));
      this.count = this.entityManager.createQuery(countCriteria)
            .getSingleResult();

      // Populate this.pageItems

      CriteriaQuery<User> criteria = builder.createQuery(User.class);
      root = criteria.from(User.class);
      TypedQuery<User> query = this.entityManager.createQuery(criteria
            .select(root).where(getSearchPredicates(root)));
      query.setFirstResult(this.page * getPageSize()).setMaxResults(
            getPageSize());
      this.pageItems = query.getResultList();
   }

   private Predicate[] getSearchPredicates(Root<User> root)
   {

      CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
      List<Predicate> predicatesList = new ArrayList<Predicate>();

      String name = this.example.getName();
      if (name != null && !"".equals(name))
      {
         predicatesList.add(builder.like(builder.lower(root.<String> get("name")), '%' + name.toLowerCase() + '%'));
      }
      String email = this.example.getEmail();
      if (email != null && !"".equals(email))
      {
         predicatesList.add(builder.like(builder.lower(root.<String> get("email")), '%' + email.toLowerCase() + '%'));
      }
      String login = this.example.getLogin();
      if (login != null && !"".equals(login))
      {
         predicatesList.add(builder.like(builder.lower(root.<String> get("login")), '%' + login.toLowerCase() + '%'));
      }
      String password = this.example.getPassword();
      if (password != null && !"".equals(password))
      {
         predicatesList.add(builder.like(builder.lower(root.<String> get("password")), '%' + password.toLowerCase() + '%'));
      }

      return predicatesList.toArray(new Predicate[predicatesList.size()]);
   }

   public List<User> getPageItems()
   {
      return this.pageItems;
   }

   public long getCount()
   {
      return this.count;
   }

   /*
    * Support listing and POSTing back User entities (e.g. from inside an
    * HtmlSelectOneMenu)
    */

   public List<User> getAll()
   {

      CriteriaQuery<User> criteria = this.entityManager
            .getCriteriaBuilder().createQuery(User.class);
      return this.entityManager.createQuery(
            criteria.select(criteria.from(User.class))).getResultList();
   }

   @Resource
   private SessionContext sessionContext;

   public Converter getConverter()
   {

      final UserBean ejbProxy = this.sessionContext.getBusinessObject(UserBean.class);

      return new Converter()
      {

         @Override
         public Object getAsObject(FacesContext context,
               UIComponent component, String value)
         {

            return ejbProxy.findById(Integer.valueOf(value));
         }

         @Override
         public String getAsString(FacesContext context,
               UIComponent component, Object value)
         {

            if (value == null)
            {
               return "";
            }

            return String.valueOf(((User) value).getId());
         }
      };
   }

   /*
    * Support adding children to bidirectional, one-to-many tables
    */

   private User add = new User();

   public User getAdd()
   {
      return this.add;
   }

   public User getAdded()
   {
      User added = this.add;
      this.add = new User();
      return added;
   }
}
