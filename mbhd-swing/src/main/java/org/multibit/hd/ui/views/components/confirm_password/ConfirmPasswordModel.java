package org.multibit.hd.ui.views.components.confirm_password;

import org.multibit.hd.ui.models.Model;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Model to provide the following to view:</p>
 * <ul>
 * <li>Show/hide the seed phrase (initially hidden)</li>
 * </ul>
 *
 * @since 0.0.1
 *  
 */
public class ConfirmPasswordModel implements Model<String> {

  private char[] password2;
  private char[] password1;

  private final String panelName;

  /**
   * @param panelName The panel name to identify the "verification status" and "next" buttons
   */
  public ConfirmPasswordModel(String panelName) {
    this.panelName = panelName;
  }

  /**
   * <p>Compares the passwords and fires a credentials status event</p>
   */
  public boolean comparePasswords() {

    final boolean passwordsEqual;

    if (password1 == null || password2 == null) {
      passwordsEqual = false;
    } else if (password1.length == 0 || password2.length == 0) {
      passwordsEqual = false;
    } else if (password1.length != password2.length) {
      passwordsEqual = false;
    }

    else {

      // Time-constant comparison (overkill but useful exercise)
      int result = 0;
      for (int i = 0; i < password1.length; i++) {
        result |= password1[i] ^ password2[i];
      }

      // Check for a match
      passwordsEqual = (result == 0);
    }

    // TODO Consider a check for whitespace characters leading or trailing

    //both passwords should be equal
    if(!passwordsEqual)
    return false;

    //the length of the password should be at least 8 characters
    if(password1.length<8)
      return false;

    //not allowed to have whitespace characters leading or trailing in the password
    if( password1[0]==' ' || password1[password1.length-1]==' ')
      return false;

      boolean containsDigit=false;
      boolean containsLowercaseLetter=false;
      boolean containsUppercaseLetter=false;

      for(int i=0; i<password1.length; i++)
      {
        if(password1[i]>=48 && password1[i]<=57)
        containsDigit=true;

        else if(password1[i]>=65 && password1[i]<=90)
          containsUppercaseLetter=true;

        else if(password1[i]>=97 && password1[i]<=122)
          containsLowercaseLetter=true;

      }

      //the password should contain at least one uppercase letter, one lower case letter, and one digit.
      if(!containsUppercaseLetter || !containsLowercaseLetter || !containsDigit)
       return false;

    //the password should contain at least one special character or symbol
    Pattern p = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
    Matcher m = p.matcher(new String(password1));
    boolean containsSymbol = m.find();
    if(!containsSymbol)
      return false;



   return true;
  }

  /**
   * @return The panel name that this component is associated with
   */
  public String getPanelName() {
    return panelName;
  }

  @Override
  public String getValue() {
    return String.valueOf(password1);
  }

  @Override
  public void setValue(String value) {

    this.password1 = value.toCharArray();

  }

  public void setPassword1(char[] password1) {
    this.password1 = password1;
  }

  public void setPassword2(char[] password2) {
    this.password2 = password2;
  }
}
