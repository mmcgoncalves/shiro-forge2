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

import org.shiro.forge2.model.UserRole;
import org.shiro.forge2.model.Role;
import org.shiro.forge2.model.User;

/**
 * Backing bean for UserRole entities.
 * <p/>
 * This class provides CRUD functionality for all UserRole entities. It focuses
 * purely on Java EE 6 standards (e.g. <tt>&#64;ConversationScoped</tt> for
 * state management, <tt>PersistenceContext</tt> for persistence,
 * <tt>CriteriaBuilder</tt> for searches) rather than introducing a CRUD framework or
 * custom base class.
 */

@Named
@Stateful
@ConversationScoped
public class UserRoleBean implements Serializable
{

   private static final long serialVersionUID = 1L;

   /*
    * Support creating and retrieving UserRole entities
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

   private UserRole userRole;

   public UserRole getUserRole()
   {
      return this.userRole;
   }

   public void setUserRole(UserRole userRole)
   {
      this.userRole = userRole;
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
         this.userRole = this.example;
      }
      else
      {
         this.userRole = findById(getId());
      }
   }

   public UserRole findById(Integer id)
   {

      return this.entityManager.find(UserRole.class, id);
   }

   /*
    * Support updating and deleting UserRole entities
    */

   public String update()
   {
      this.conversation.end();

      try
      {
         if (this.id == null)
         {
            this.entityManager.persist(this.userRole);
            return "search?faces-redirect=true";
         }
         else
         {
            this.entityManager.merge(this.userRole);
            return "view?faces-redirect=true&id=" + this.userRole.getId();
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
         UserRole deletableEntity = findById(getId());

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
    * Support searching UserRole entities with pagination
    */

   private int page;
   private long count;
   private List<UserRole> pageItems;

   private UserRole example = new UserRole();

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

   public UserRole getExample()
   {
      return this.example;
   }

   public void setExample(UserRole example)
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
      Root<UserRole> root = countCriteria.from(UserRole.class);
      countCriteria = countCriteria.select(builder.count(root)).where(
            getSearchPredicates(root));
      this.count = this.entityManager.createQuery(countCriteria)
            .getSingleResult();

      // Populate this.pageItems

      CriteriaQuery<UserRole> criteria = builder.createQuery(UserRole.class);
      root = criteria.from(UserRole.class);
      TypedQuery<UserRole> query = this.entityManager.createQuery(criteria
            .select(root).where(getSearchPredicates(root)));
      query.setFirstResult(this.page * getPageSize()).setMaxResults(
            getPageSize());
      this.pageItems = query.getResultList();
   }

   private Predicate[] getSearchPredicates(Root<UserRole> root)
   {

      CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
      List<Predicate> predicatesList = new ArrayList<Predicate>();

      Role role = this.example.getRole();
      if (role != null)
      {
         predicatesList.add(builder.equal(root.get("role"), role));
      }
      User user = this.example.getUser();
      if (user != null)
      {
         predicatesList.add(builder.equal(root.get("user"), user));
      }

      return predicatesList.toArray(new Predicate[predicatesList.size()]);
   }

   public List<UserRole> getPageItems()
   {
      return this.pageItems;
   }

   public long getCount()
   {
      return this.count;
   }

   /*
    * Support listing and POSTing back UserRole entities (e.g. from inside an
    * HtmlSelectOneMenu)
    */

   public List<UserRole> getAll()
   {

      CriteriaQuery<UserRole> criteria = this.entityManager
            .getCriteriaBuilder().createQuery(UserRole.class);
      return this.entityManager.createQuery(
            criteria.select(criteria.from(UserRole.class))).getResultList();
   }

   @Resource
   private SessionContext sessionContext;

   public Converter getConverter()
   {

      final UserRoleBean ejbProxy = this.sessionContext.getBusinessObject(UserRoleBean.class);

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

            return String.valueOf(((UserRole) value).getId());
         }
      };
   }

   /*
    * Support adding children to bidirectional, one-to-many tables
    */

   private UserRole add = new UserRole();

   public UserRole getAdd()
   {
      return this.add;
   }

   public UserRole getAdded()
   {
      UserRole added = this.add;
      this.add = new UserRole();
      return added;
   }
}
