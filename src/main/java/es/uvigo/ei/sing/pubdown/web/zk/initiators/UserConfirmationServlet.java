package es.uvigo.ei.sing.pubdown.web.zk.initiators;

import java.io.IOException;

import javax.persistence.NoResultException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import es.uvigo.ei.sing.pubdown.web.entities.Registration;
import es.uvigo.ei.sing.pubdown.web.entities.User;
import es.uvigo.ei.sing.pubdown.web.zk.util.CleanEntityManagerTransactionManager;
import es.uvigo.ei.sing.pubdown.web.zk.util.TransactionManager;

/**
 * Servlet to manage the {@link User} registration requests
 */
@WebServlet("/confirmation")
public class UserConfirmationServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private final TransactionManager tm = new CleanEntityManagerTransactionManager();

	/**
	 * If the request does not have the uuid parameter or its null, it will
	 * redirect to the index page. If the uuid parameter does not exist in the
	 * DB {@link Registration} table , it will redirect to the confirmationError
	 * page. If the request uuid parameter exists in the DB {@link Registration}
	 * table, it will persist the {@link User} in the DB {@link User} table and
	 * it will remove the corresponding {@link Registration} and redirect to the
	 * confirmation page.
	 */
	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {
		final String uuid = req.getParameter("uuid");
		if (uuid == null || uuid.isEmpty()) {
			resp.sendRedirect("index.zul");
		} else {
			final Registration registration = checkIfUuidExists(uuid);
			if (registration != null) {
				final User user = registration.getUser();
				tm.runInTransaction(em -> {
					em.remove(registration);
					em.persist(user);
					try {
						resp.sendRedirect("confirmation.zul");
					} catch (final IOException e) {
						e.printStackTrace();
					}
				});
			} else {
				resp.sendRedirect("confirmationError.zul");
			}
		}
	}

	/**
	 * Checks if the uuid exits in the DB {@link Registration} table
	 * 
	 * @param uuid
	 *            the {@link Registration} uuid
	 * @return a {@link Registration} if its founded, <code>null</code>
	 *         otherwise
	 */
	private Registration checkIfUuidExists(final String uuid) {
		try {
			return tm.get(em -> em.createQuery("SELECT u FROM Registration u WHERE u.uuid = :uuid", Registration.class)
					.setParameter("uuid", uuid).getSingleResult());
		} catch (final NoResultException e) {
			return null;
		}
	}
}