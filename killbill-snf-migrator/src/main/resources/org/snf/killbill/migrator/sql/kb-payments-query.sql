SELECT
	p.id AS pmt_id
	, p.effective_date AS pmt_date
	, p.amount AS pmt_amount
FROM payment_transactions p
WHERE p.account_record_id = ?
ORDER BY p.effective_date