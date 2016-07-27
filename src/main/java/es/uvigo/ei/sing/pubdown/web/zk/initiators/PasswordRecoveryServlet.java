package es.uvigo.ei.sing.pubdown.web.zk.initiators;

import java.io.IOException;

import javax.persistence.NoResultException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import es.uvigo.ei.sing.pubdown.web.entities.PasswordRecovery;
import es.uvigo.ei.sing.pubdown.web.entities.User;
import es.uvigo.ei.sing.pubdown.web.zk.util.CleanEntityManagerTransactionManager;
import es.uvigo.ei.sing.pubdown.web.zk.util.TransactionManager;

/**
 * Servlet to manage the {@link User} password recovery requests
 */
@WebServlet("/recovery")
public class PasswordRecoveryServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final String CHANGE_PASSWORD = "passwordRecovery.zul?uuid=";

	private final TransactionManager tm = new CleanEntityManagerTransactionManager();

	/**
	 * If the request does not have the uuid parameter, is null or does not
	 * exist in the DB {@link PasswordRecovery} table, it will redirect to the
	 * index page. If the request uuid parameter exists in the DB
	 * {@link PasswordRecovery} table, it will redirect to the passwordRecovery
	 * page.
	 */
	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {
		final String uuid = req.getParameter("uuid");
		if (uuid == null || uuid.isEmpty()) {
			resp.sendRedirect("index.zul");
		} else {
			final PasswordRecovery passwordRecovery = checkIfUuidExists(uuid);
			if (passwordRecovery != null) {
				resp.sendRedirect(CHANGE_PASSWORD + passwordRecovery.getUuid());
			} else {
				resp.sendRedirect("index.zul");
			}
		}
	}

	/**
	 * Checks if the uuid exits in the DB {@link PasswordRecovery} table
	 * 
	 * @param uuid
	 *            the {@link PasswordRecovery} uuid
	 * @return a {@link PasswordRecovery} if its founded, <code>null</code>
	 *         otherwise
	 */
	private PasswordRecovery checkIfUuidExists(final String uuid) {
		try {
			return tm.get(em -> em
					.createQuery("SELECT u FROM PasswordRecovery u WHERE u.uuid = :uuid", PasswordRecovery.class)
					.setParameter("uuid", uuid).getSingleResult());
		} catch (final NoResultException e) {
			e.printStackTrace();
			return null;
		}
	}
}
