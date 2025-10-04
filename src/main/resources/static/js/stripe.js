const stripe = Stripe('pk_test_51SBSQlLtmlk1U6vF5WwdS02C96ns1MNV3YiRLJnp91psgTPXtEGEHLz5ggd5sJDjRN2cSi4XWctRVWfuQUpMlTIs00IgRyMuYc');
const paymentButton = document.querySelector('#paymentButton');

paymentButton.addEventListener('click', () => {
	stripe.redirectToCheckout({
		sessionId: sessionId	
	})
});