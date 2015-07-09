package org.shiro.forge2.model;

// Generated 09/07/2015 11:15:53 by Hibernate Tools 4.3.1

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.lang.Override;

/**
 * UserRole generated by hbm2java
 */
@Entity
@Table(name = "user_role"
      , catalog = "shiro_forge2")
public class UserRole implements java.io.Serializable
{

   /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
private Integer id;
   private Role role;
   private User user;

   public UserRole()
   {
   }

   public UserRole(Role role, User user)
   {
      this.role = role;
      this.user = user;
   }

   @Id
   @GeneratedValue(strategy = IDENTITY)
   @Column(name = "id", unique = true, nullable = false)
   public Integer getId()
   {
      return this.id;
   }

   public void setId(Integer id)
   {
      this.id = id;
   }

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "role_id", nullable = false)
   public Role getRole()
   {
      return this.role;
   }

   public void setRole(Role role)
   {
      this.role = role;
   }

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "user_id", nullable = false)
   public User getUser()
   {
      return this.user;
   }

   public void setUser(User user)
   {
      this.user = user;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
      {
         return true;
      }
      if (!(obj instanceof UserRole))
      {
         return false;
      }
      UserRole other = (UserRole) obj;
      if (id != null)
      {
         if (!id.equals(other.id))
         {
            return false;
         }
      }
      return true;
   }

}