# User Usage Manual
## Institutional Accounts Payable, Accounts Receivable, Fees Collection, Patient Debt, Supplier Credit, and Financial Reporting Application

This manual explains how the different types of users should use the application. It is written for end users, supervisors, finance officers, and control users.

The application supports decentralized transaction entry by schools, hospitals, health centers, pharmacies, procurement units, and administrative departments, while allowing the central accounting office to review, approve, consolidate, reconcile, and report institutional financial activity.

---

# 1. Purpose of This Manual

This manual helps users understand:

1. What their role allows them to do.
2. Which screens they should use.
3. How to perform common daily, weekly, and monthly tasks.
4. How to enter financial data correctly.
5. How to avoid common errors.
6. How to maintain proper audit trails and financial controls.

The application may include several modules depending on institutional configuration:

- Accounts Payable
- Accounts Receivable
- School fees collection
- Patient billing and insurance claims
- Supplier credits and procurement finance
- Inventory and pharmacy stock finance
- Treasury and bank reconciliation
- Payroll
- Fixed assets
- Budgeting and grants
- Internal billing and cost allocation
- General ledger and accounting entries
- Period close and financial reporting
- Audit and control

Your actual access depends on your assigned role and privileges.

---

# 2. Types of Users

The application supports the following major user types:

| Role | Primary Responsibility |
|---|---|
| System Administrator | Manages users, roles, privileges, departments, settings, and system configuration |
| Central Accounting Manager | Oversees institutional accounting, consolidation, period close, approvals, and financial control |
| Central Accounting Officer | Reviews departmental submissions, validates transactions, performs reconciliations, and prepares reports |
| Department Manager | Approves departmental transactions and monitors departmental balances |
| Department Finance Officer | Enters departmental bills, invoices, charges, receipts, payments, and submissions |
| School Fees Officer | Manages students, sponsors, fee schedules, student charges, scholarships, waivers, receipts, and fee collections |
| Hospital Billing Officer | Manages patient accounts, encounters, charges, insurance claims, payments, subsidies, and patient debt |
| Pharmacy or Medical Store Officer | Manages drug suppliers, medical supply suppliers, receipts, supplier credits, batches, and pharmacy reconciliation |
| Procurement Officer | Manages purchase requests, purchase orders, goods receipts, supplier invoice matching, and procurement commitments |
| Auditor | Reviews transactions, reports, reconciliations, and audit logs without changing financial data |
| Viewer | Views permitted dashboards and reports only |

Institutions may combine roles for small teams, but sensitive duties should be separated where possible.

---

# 3. Getting Started

## 3.1 Logging In

1. Open the application URL provided by your institution.
2. Enter your username.
3. Enter your password.
4. Click **Log In**.

If your account is locked after too many failed attempts, contact the System Administrator.

## 3.2 First Login

After logging in, confirm:

- Your name and role.
- Your assigned department or institution.
- Your current fiscal period.
- Your default currency.
- Your accessible modules.

If your department is incorrect, do not proceed with financial entry. Contact your supervisor or System Administrator.

## 3.3 Session Timeout

For security, the application may automatically log you out after inactivity.

Save your work frequently, especially when entering:

- Bills
- Invoices
- Student charges
- Patient charges
- Payments
- Receipts
- Journal adjustments

---

# 4. General Navigation

The application uses a desktop-style layout.

## 4.1 Sidebar Navigation

The sidebar contains grouped sections such as:

- Dashboard
- Receivables
- Payables and Suppliers
- Cash and Treasury
- Budget and Grants
- Payroll
- Fixed Assets
- Inventory
- Internal Billing
- Accounting
- Central Control
- Reporting
- Administration

Use the sidebar to move between major areas.

## 4.2 Page Header

Most screens include:

- Page title
- Short description
- Toolbar buttons
- Breadcrumbs
- Current department or facility context

## 4.3 Grids and Lists

Most list screens support:

- Searching
- Filtering
- Sorting
- Date-range selection
- Status filtering
- Department filtering
- Pagination
- Column selection
- Export
- Refresh

Use filters before exporting data.

## 4.4 Save and Discard Controls

Some forms and editable grids show:

- **Save**
- **Discard**
- **Unsaved changes** indicator

If you see **Unsaved changes**, do not navigate away until you save or discard.

## 4.5 Status Badges

Common statuses include:

| Status | Meaning |
|---|---|
| Draft | Not yet submitted |
| Submitted | Sent for approval or central review |
| Pending Approval | Waiting for authorized approval |
| Approved | Accepted for posting or payment |
| Posted | Recorded in the accounting system |
| Partially Paid | Some payment has been received or made |
| Paid | Fully settled |
| Overdue | Due date has passed and balance remains |
| Rejected | Not accepted |
| Cancelled | Transaction cancelled |
| Locked | Period or record cannot be changed |

Do not rely on color alone. Read the status text.

---

# 5. Common Rules for All Users

All users should follow these rules:

1. Do not create duplicate records.
2. Do not use personal names as account identifiers where an official number exists.
3. Always select the correct department.
4. Always select the correct date.
5. Always confirm the correct currency.
6. Do not post transactions into closed or locked periods.
7. Do not delete financial records. Use cancellation, reversal, adjustment, or write-off workflows.
8. Always upload supporting documents where required.
9. Always provide reasons for corrections, reversals, waivers, write-offs, and adjustments.
10. Always review totals before submitting.

---

# 6. System Administrator Usage Guide

## 6.1 Role of the System Administrator

The System Administrator manages the technical and organizational configuration of the application.

This role should not normally create or approve financial transactions unless also assigned a finance role.

## 6.2 Main Responsibilities

The System Administrator is responsible for:

1. Creating and disabling user accounts.
2. Assigning roles and privileges.
3. Maintaining departments, facilities, locations, and regions.
4. Maintaining institutional settings.
5. Configuring currencies and exchange rates.
6. Configuring tax codes where delegated.
7. Monitoring audit logs.
8. Managing import/export permissions.
9. Supporting scheduled jobs and system health.
10. Ensuring security settings are enforced.

## 6.3 User Management

To add a user:

1. Open **Administration** → **Users**.
2. Click **New User**.
3. Enter:
    - Username
    - Full name
    - Email
    - Department
    - Role
    - Privileges if applicable
4. Set active status.
5. Save.

To disable a user:

1. Open the user record.
2. Set the user to inactive.
3. Save.

Do not delete users who have performed financial transactions. Disable them instead so the audit trail remains intact.

## 6.4 Role and Privilege Assignment

Assign only the minimum privileges required.

Examples:

- School Fees Officer should receive education finance privileges.
- Hospital Billing Officer should receive health billing privileges.
- Central Accounting Manager should receive period close and consolidation privileges.
- Auditors should receive read-only access.
- Viewers should receive report access only.

Avoid giving broad administrative privileges to operational users.

## 6.5 Department and Location Maintenance

Before departments can transact, ensure each department has:

- Department code
- Department name
- Department type
- Parent department
- Region
- Location
- Manager
- Finance officer
- Active status

Examples of department types:

- School
- Hospital
- Health Center
- Pharmacy
- Headquarters
- Procurement
- Logistics
- Finance Office

## 6.6 Period and System Settings

The System Administrator should verify:

- Base currency
- Fiscal year start month
- Time zone
- Date format
- Password policy
- Session timeout
- Scheduled job settings
- Email notification settings
- File upload limits

## 6.7 Audit Log Review

The System Administrator may review:

- Failed login attempts
- User role changes
- Password resets
- Settings changes
- File upload or deletion events
- Privilege changes

If suspicious activity is detected, escalate according to institutional policy.

## 6.8 Daily Tasks

- Check failed login alerts if enabled.
- Verify scheduled jobs completed successfully.
- Review user access requests.

## 6.9 Monthly Tasks

- Review inactive users.
- Review excessive privileges.
- Confirm department structures are current.
- Confirm new facilities have correct reporting relationships.

---

# 7. Central Accounting Manager Usage Guide

## 7.1 Role of the Central Accounting Manager

The Central Accounting Manager is responsible for institutional financial oversight.

This user ensures that departmental transactions are reviewed, approved, consolidated, reconciled, and closed according to policy.

## 7.2 Main Responsibilities

- Review institution-wide dashboards
- Approve or reject departmental submissions
- Post transactions to central accounting
- Reclassify incorrect coding
- Manage accounting periods
- Run period close checklist
- Soft close and hard close periods
- Lock periods
- Review reconciliations
- Review financial statements
- Monitor unposted and erroneous transactions
- Monitor budget utilization where enabled

## 7.3 Institution Dashboard

The Central Accounting Manager should begin with the institution dashboard.

Review:

- Total accounts payable
- Total accounts receivable
- Student fees outstanding
- Patient debt outstanding
- Insurance receivables
- Supplier credits
- Overdue balances
- Cash-flow forecast
- Department submission status
- Pending approvals
- Unreconciled items

## 7.4 Reviewing Departmental Submissions

To review a department submission:

1. Open **Central Control** → **Department Submissions**.
2. Select the reporting period.
3. Select the department.
4. Review:
    - Opening balances
    - New transactions
    - Payments or collections
    - Closing balances
    - Transaction count
    - Supporting documents
    - Reconciliation status
5. Compare department totals with central records.
6. Accept, reject, or return for correction.

### Accepting a Submission

Accept only if:

- Totals reconcile.
- Supporting documents exist.
- Transactions belong to the correct period.
- No duplicate references exist.
- No negative balances are unexplained.
- Accounting coding appears correct.

### Rejecting or Returning a Submission

If there are errors:

1. Click **Reject** or **Return for Correction**.
2. Enter a clear reason.
3. Specify what the department must correct.

Avoid vague messages such as “incorrect” or “check again.”

Use clear instructions such as:

> “Three student receipts are missing cashier references. Please correct receipt numbers RCT-0012, RCT-0014, and RCT-0015 and resubmit.”

## 7.5 Period Close

The Central Accounting Manager controls period close.

### Soft Close

Soft close is used when normal transaction entry should stop but limited central corrections may still be allowed.

Use soft close after:

- Department submissions are reviewed.
- Reconciliations are run.
- Trial balance is checked.
- Financial statement validation is completed.

### Hard Close

Hard close is stronger. It prevents normal posting and should be used once the period is finalized.

### Lock

Lock is the final state. Locked periods should not be reopened except under approved exception procedures.

To close a period:

1. Open **Accounting** → **Period Close**.
2. Select the period.
3. Run the close checklist.
4. Review validations.
5. Resolve failed validations.
6. Soft close the period.
7. Hard close the period if required.
8. Lock the period if final.

## 7.6 Financial Statement Validation

Before closing, run validations for:

- Trial balance balance
- Balance sheet equation
- Statement of activity consistency
- Sub-ledger to general ledger reconciliation
- Payment allocation exceptions
- Negative balances
- Unposted transactions

Do not close a period while critical validations fail.

## 7.7 Reclassifications and Corrections

If a transaction is posted to the wrong account, department, fund, or cost center:

1. Locate the transaction or journal entry.
2. Do not edit a posted entry directly.
3. Use reversal, adjustment, or replacement workflow.
4. Enter the reason.
5. Post the correction.
6. Review the audit trail.

## 7.8 Reports

The Central Accounting Manager frequently uses:

- Consolidated AP report
- Consolidated AR report
- Trial balance
- General ledger
- Department balance summary
- Fund utilization report
- Grant utilization report
- Cash-flow forecast
- Financial statements
- Period movement report
- Reconciliation discrepancy report

## 7.9 Monthly Tasks

- Review all department submissions.
- Run reconciliations.
- Review unallocated receipts.
- Review overdue balances.
- Validate trial balance.
- Close the period.
- Produce consolidated reports.

---

# 8. Central Accounting Officer Usage Guide

## 8.1 Role of the Central Accounting Officer

The Central Accounting Officer supports the Central Accounting Manager by reviewing transactions, validating coding, preparing reconciliations, and generating reports.

This role may have posting rights within assigned limits.

## 8.2 Main Responsibilities

- Review submitted departmental transactions
- Validate account coding and dimensions
- Request corrections
- Reconcile sub-ledgers to general ledger
- Monitor unposted transactions
- Prepare reports
- Support period close

## 8.3 Reviewing Transactions

When reviewing transactions, check:

1. Correct department.
2. Correct date and accounting period.
3. Correct currency and exchange rate.
4. Correct service category or expense category.
5. Correct fund, project, or cost center.
6. Supporting documentation.
7. Approval status.
8. Duplicate references.
9. Unusual amounts.

## 8.4 Requesting Corrections

If a transaction is incorrect:

1. Open the transaction.
2. Select **Return for Correction** or **Request Correction**.
3. Enter the required correction.
4. Save.

Examples of correction requests:

> “Please attach the goods receipt before this supplier invoice can be accepted.”

> “Please allocate this receipt to the correct student charge.”

> “Please correct the expense category from Office Supplies to Medical Supplies.”

## 8.5 Reconciliation Work

The Central Accounting Officer should run or review:

- Student fees to general ledger reconciliation
- Patient debt to general ledger reconciliation
- Insurance receivable reconciliation
- Supplier payable reconciliation
- Treasury and bank reconciliation
- Internal billing reconciliation
- Inventory valuation reconciliation

For each reconciliation:

1. Open the reconciliation screen.
2. Select the period.
3. Run the reconciliation.
4. Review differences.
5. Drill down into source transactions.
6. Identify whether the difference is due to:
    - Missing posting
    - Duplicate posting
    - Wrong account
    - Wrong period
    - Unallocated payment
    - Timing difference
    - Data entry error
7. Document the explanation.

## 8.6 Daily Tasks

- Review newly submitted transactions.
- Follow up on returned transactions.
- Check unallocated receipts.
- Monitor failed posting errors.

## 8.7 Monthly Tasks

- Support period close.
- Prepare reconciliation summaries.
- Generate trial balance and ledger reports.
- Identify recurring errors by department.

---

# 9. Department Manager Usage Guide

## 9.1 Role of the Department Manager

The Department Manager is responsible for the financial activity of a specific department, such as a school, hospital, health center, pharmacy, or administrative unit.

This user approves departmental transactions before they are sent to central accounting.

## 9.2 Main Responsibilities

- Approve departmental bills, invoices, charges, and payments
- Monitor departmental receivables and payables
- Monitor collections and supplier obligations
- Review departmental submission before central submission
- Ensure supporting documents are attached
- Ensure staff follow proper procedures

## 9.3 Department Dashboard

The Department Manager should review:

- Department accounts payable
- Department accounts receivable
- Outstanding balances
- Overdue balances
- Payments collected
- Payments due
- Transactions awaiting approval
- Transactions returned for correction
- Submission status
- Reconciliation status

## 9.4 Approving Transactions

To approve a transaction:

1. Open the relevant module:
    - Bills
    - Invoices
    - Student charges
    - Patient charges
    - Supplier invoices
    - Payments
2. Filter by status **Pending Approval**.
3. Open the transaction.
4. Check:
    - Amount
    - Department
    - Date
    - Vendor, customer, student, patient, or supplier
    - Supporting documents
    - Budget availability if applicable
5. Approve or reject.

### Approval Criteria

Approve only if:

- The transaction is legitimate.
- The amount is correct.
- The transaction belongs to your department.
- Supporting documents exist.
- The transaction complies with policy.
- The correct approval limit is observed.

### Rejection

If rejecting:

1. Select **Reject**.
2. Provide a clear reason.
3. Return the transaction to the preparer if needed.

## 9.5 Reviewing Department Submission

Before the department submits to central accounting, the Department Manager should verify:

- Opening balances are correct.
- All transactions are recorded.
- Payments and collections are complete.
- No unexplained negative balances exist.
- No duplicate transaction numbers exist.
- Returned transactions have been corrected.
- Supporting schedules are attached.

## 9.6 Monitoring Collections

For schools and hospitals, the Department Manager should monitor:

- Student fee collection rate
- Outstanding student balances
- Patient collections
- Insurance claim status
- Overdue patient debt
- Waivers and scholarships
- Daily cashier reconciliation

## 9.7 Monitoring Supplier Obligations

For departments with procurement activity, monitor:

- Outstanding supplier credits
- Drug supplier balances
- Medical supply supplier balances
- Overdue invoices
- Purchase order commitments
- Unmatched invoices
- Payments due

## 9.8 Weekly Tasks

- Review awaiting approvals.
- Review overdue receivables.
- Review supplier payment due dates.
- Follow up on returned transactions.

## 9.9 Monthly Tasks

- Approve departmental submission.
- Review department reconciliation.
- Confirm collection performance.
- Confirm supplier balances.

---

# 10. Department Finance Officer Usage Guide

## 10.1 Role of the Department Finance Officer

The Department Finance Officer enters and maintains financial transactions for the department.

This role is central to day-to-day financial operations.

## 10.2 Main Responsibilities

- Enter bills and invoices
- Enter charges and receipts
- Record payments
- Upload supporting documents
- Prepare departmental submission
- Reconcile local records
- Correct returned transactions
- Generate departmental reports

## 10.3 Entering a Bill or Supplier Invoice

To enter a bill:

1. Open **Payables** → **Bills** or **Supplier Invoices**.
2. Click **New Bill**.
3. Select:
    - Supplier
    - Department
    - Bill date
    - Due date
    - Currency
    - Purchase order if applicable
    - Expense category
    - Fund or project if required
4. Enter line items:
    - Description
    - Quantity
    - Unit price
    - Tax code
    - Cost center
5. Review calculated totals.
6. Upload supporting documents.
7. Save as draft.
8. Submit for approval.

### Checks Before Submission

Confirm:

- Supplier name is correct.
- Invoice number is not duplicate.
- Invoice date is correct.
- Due date is correct.
- Amounts are correct.
- Tax is correct.
- Goods receipt exists if required.
- Purchase order reference exists if required.

## 10.4 Entering a Receivable Invoice or Charge

To enter a receivable:

1. Open the relevant receivable module.
2. Select the customer, student, patient, sponsor, or payer.
3. Enter service details.
4. Enter amount and due date.
5. Apply discount, scholarship, waiver, subsidy, or insurance coverage where applicable.
6. Save.
7. Submit for approval if required.

## 10.5 Recording a Payment or Receipt

To record a payment:

1. Open the relevant payment or receipt screen.
2. Select the department.
3. Select the payer or payee.
4. Enter:
    - Amount
    - Payment date
    - Payment method
    - Reference number
    - Currency
5. Allocate the payment to outstanding transactions.
6. Confirm that allocation does not exceed payment amount.
7. Save.
8. Post.

### Allocation Rules

- Do not allocate more than the payment amount.
- Do not allocate more than the transaction balance.
- Leave unallocated amount only if the payment is genuinely on account.
- Review unallocated amounts frequently.

## 10.6 Correcting Returned Transactions

If central accounting returns a transaction:

1. Open the returned transaction.
2. Read the central accounting comment.
3. Correct the error.
4. Add missing documents if required.
5. Resubmit.

Do not create a duplicate transaction unless instructed and authorized.

## 10.7 Preparing Department Submission

At the end of the reporting period:

1. Ensure all transactions are entered.
2. Ensure all returned items are corrected.
3. Run department reports.
4. Reconcile totals.
5. Prepare the submission.
6. Send to Department Manager for approval.

## 10.8 Daily Tasks

- Enter new bills and invoices.
- Record receipts and payments.
- Upload documents.
- Follow up on unallocated receipts.
- Check returned transactions.

## 10.9 Monthly Tasks

- Prepare departmental submission.
- Reconcile department balances.
- Support audit queries.
- Review outstanding balances.

---

# 11. School Fees Officer Usage Guide

## 11.1 Role of the School Fees Officer

The School Fees Officer manages student billing and fee collection for schools, colleges, and training centers.

## 11.2 Main Responsibilities

- Maintain student records
- Maintain guardians, parents, and sponsors
- Configure fee schedules
- Generate student charges
- Apply scholarships, discounts, and waivers
- Record fee payments
- Issue receipts
- Monitor overdue fees
- Reconcile daily collections
- Produce fee reports

## 11.3 Managing Students

To add a student:

1. Open **Education** → **Students**.
2. Click **New Student**.
3. Enter:
    - Student number
    - Full name
    - School or department
    - Grade, class, program, or course
    - Academic year
    - Term
    - Enrollment status
    - Guardian or sponsor
4. Save.

Use the official student number as the primary identifier.

Do not rely on names alone.

## 11.4 Managing Guardians, Parents, and Sponsors

A student may have multiple sponsors.

For each sponsor, record:

- Name
- Relationship
- Organization if applicable
- Phone
- Email
- Billing responsibility
- Preferred payment method

One sponsor may pay for multiple students. When receiving a sponsor payment, allocate it carefully across the correct student charges.

## 11.5 Fee Schedules

Fee schedules define the fees to be charged.

They may be configured by:

- School
- Academic year
- Term
- Grade
- Class
- Program
- Student category
- Boarding status
- Residency status

Before generating charges, confirm:

- Academic year is correct.
- Term is correct.
- Fee types are correct.
- Amounts are approved.
- Due dates are correct.
- Mandatory and optional fees are correctly marked.

## 11.6 Generating Student Charges

To generate charges from a fee schedule:

1. Open **Education** → **Fee Schedules**.
2. Select the fee schedule.
3. Choose the relevant students or enrollment group.
4. Generate charges.
5. Review generated charges.
6. Confirm totals.

Do not generate the same fee schedule twice unless correcting an error.

## 11.7 Applying Scholarships, Discounts, and Waivers

Scholarships, discounts, and waivers must never silently reduce the original fee.

Each adjustment must show:

- Original charge
- Adjustment type
- Adjustment amount
- Reason
- Approval reference
- Net receivable amount

To apply an adjustment:

1. Open the student charge.
2. Select **Apply Adjustment**.
3. Choose:
    - Scholarship
    - Discount
    - Waiver
    - Subsidy if applicable
4. Enter amount or percentage.
5. Enter reason.
6. Attach approval document if required.
7. Save.

Only authorized users may approve adjustments.

## 11.8 Recording Student Payments

To record a student payment:

1. Open **Education** → **Student Charges** or **Student Payments**.
2. Select the student or sponsor.
3. Select the payment source.
4. Enter amount.
5. Select payment method.
6. Allocate to outstanding charges.
7. Generate receipt.
8. Save and post.

### Payment Allocation Tips

- Allocate oldest fees first unless policy says otherwise.
- Confirm the student name and number before posting.
- If a sponsor pays for multiple students, allocate across all applicable charges.
- If a payment is not fully allocated, leave the remainder as unallocated credit and explain why.

## 11.9 Daily Cashier Reconciliation

At the end of each collection day:

1. Open the cashier session.
2. Review:
    - Opening balance
    - Cash received
    - Bank transfers
    - Mobile money
    - Card collections
    - Checks
    - Refunds
    - Reversals
3. Compare expected closing balance with actual closing balance.
4. Enter explanation for any variance.
5. Submit for review if required.

Do not leave unexplained variances.

## 11.10 Overdue Student Fees

Regularly review:

- Students with overdue balances
- Fees overdue by aging bucket
- Students with broken payment plans
- Sponsors with unpaid commitments
- Students with credit balances

Follow institutional policy before restricting services or releasing results.

## 11.11 Education Reports

Common reports:

- Fees billed by school
- Fees collected by school
- Outstanding fees by student
- Outstanding fees by class
- Collection rate by fee type
- Scholarship and waiver report
- Daily cashier report
- Unallocated student payments
- Student credit balances

---

# 12. Hospital Billing Officer Usage Guide

## 12.1 Role of the Hospital Billing Officer

The Hospital Billing Officer manages patient billing, insurance claims, subsidies, patient payments, and patient debt.

This role must protect confidential patient information and use patient account numbers in financial reports where possible.

## 12.2 Main Responsibilities

- Register patient billing accounts
- Record encounters for billing purposes
- Enter patient charges
- Calculate patient responsibility
- Submit insurance claims
- Record payments
- Manage payment plans
- Monitor patient debt
- Process refunds and write-offs where authorized
- Produce billing reports

## 12.3 Patient Account Registration

When creating a patient billing account:

1. Open **Health** → **Patient Accounts**.
2. Click **New Patient Account**.
3. Enter:
    - Patient number
    - Full name where required
    - Facility or department
    - Responsible payer
    - Insurance provider if applicable
    - Insurance membership number
    - Contact information
4. Save.

Use the patient account number as the primary billing identifier.

## 12.4 Patient Encounters

A patient encounter represents the visit or service episode.

Encounter types may include:

- Outpatient
- Inpatient
- Emergency
- Maternity
- Surgery
- Laboratory
- Radiology
- Pharmacy
- Dental
- Ambulance

Before charging, confirm:

- Correct patient
- Correct encounter
- Correct facility
- Correct payer
- Correct insurance provider

## 12.5 Entering Patient Charges

To enter charges:

1. Open the patient account.
2. Select the encounter.
3. Add charges:
    - Consultation
    - Admission
    - Bed
    - Nursing
    - Surgery
    - Laboratory
    - Radiology
    - Pharmacy
    - Medical supplies
    - Procedures
4. Enter quantity and unit price.
5. Apply discounts, subsidies, or insurance coverage where applicable.
6. Review:
    - Gross amount
    - Discount
    - Subsidy
    - Insurance-covered amount
    - Patient-responsible amount
    - Total amount
7. Save and post.

## 12.6 Insurance Claims

To manage insurance claims:

1. Open **Health** → **Insurance Claims**.
2. Select the patient encounter.
3. Select the payer.
4. Enter claim number.
5. Enter claimed amount.
6. Attach supporting documents.
7. Submit claim.
8. Track claim status.

Claim statuses may include:

- Draft
- Ready for Submission
- Submitted
- Under Review
- Partially Approved
- Approved
- Partially Paid
- Paid
- Rejected
- Resubmitted
- Written Off

### Rejected Claims

If a claim is rejected:

1. Read the rejection reason.
2. Correct the issue.
3. Attach additional documentation if required.
4. Resubmit if allowed.

Common rejection reasons:

- Missing insurance number
- Expired coverage
- Non-covered service
- Missing diagnosis or service code
- Duplicate claim
- Incorrect amount
- Missing supporting document

## 12.7 Patient Payments

To record a patient payment:

1. Open the patient account.
2. Select **Record Payment**.
3. Enter amount.
4. Select payment method.
5. Allocate to charges or encounters.
6. Save and generate receipt.

Do not allocate a payment above the patient’s outstanding balance.

## 12.8 Payment Plans

For patients who cannot pay immediately:

1. Open the patient account.
2. Select **Payment Plan**.
3. Enter:
    - Total debt
    - Down payment
    - Installment amount
    - Frequency
    - First due date
    - Number of installments
    - Responsible officer
4. Obtain approval if required.
5. Save.

Monitor missed installments and follow up.

## 12.9 Patient Debt Management

Review:

- Outstanding patient balances
- Aging by patient
- Aging by payer
- Aging by facility
- Insurance receivables
- Government receivables
- Disputed charges
- Credit balances
- Write-off requests

Write-offs require approval and a documented reason.

## 12.10 Daily Collections Reconciliation

At the end of each shift or day:

1. Open cashier reconciliation.
2. Review all payments received.
3. Compare expected closing balance with actual closing balance.
4. Explain variances.
5. Submit for review.

## 12.11 Health Reports

Common reports:

- Patient revenue by facility
- Patient revenue by service
- Outstanding patient debt
- Insurance receivables
- Government receivables
- Rejected claims
- Payment plan performance
- Write-offs and adjustments
- Daily collections report

---

# 13. Pharmacy or Medical Store Officer Usage Guide

## 13.1 Role of the Pharmacy or Medical Store Officer

This user manages drug and medical supply financial obligations, supplier credits, stock receipts, stock issues, batch information, and pharmacy reconciliation.

## 13.2 Main Responsibilities

- Maintain pharmaceutical supplier records
- Record purchase orders and receipts
- Track supplier invoices
- Monitor drug supplier credits
- Monitor medical supply supplier credits
- Track batch numbers and expiry dates
- Record stock receipts and issues
- Reconcile pharmacy daily activity
- Alert central accounting about overdue supplier obligations

## 13.3 Supplier Records

For each drug or medical supply supplier, confirm:

- Supplier code
- Supplier legal name
- Supplier category
- Payment terms
- Currency
- Bank details
- Contact details
- Active status

Do not use inactive suppliers for new transactions unless authorized.

## 13.4 Purchase Orders

To create or review a purchase order:

1. Open **Procurement** → **Purchase Orders**.
2. Select supplier.
3. Select department or pharmacy.
4. Enter expected delivery date.
5. Enter line items:
    - Item
    - Quantity
    - Unit price
    - Tax if applicable
6. Save.
7. Submit for approval if required.

## 13.5 Goods Receipt

When goods arrive:

1. Open the purchase order.
2. Select **Receive Goods**.
3. Enter:
    - Delivery date
    - Delivery note number
    - Quantities received
    - Accepted quantities
    - Rejected quantities
    - Batch numbers where applicable
    - Expiry dates where applicable
4. Save.

Do not receive more than ordered unless authorized.

## 13.6 Supplier Invoice Review

When a supplier invoice arrives:

1. Match the invoice to the purchase order.
2. Match the invoice to the goods receipt.
3. Confirm:
    - Supplier name
    - Invoice number
    - Invoice date
    - Due date
    - Quantity
    - Unit price
    - Total amount
    - Tax
4. Flag discrepancies.

Common discrepancies:

- Invoice price higher than purchase order
- Invoice quantity higher than received quantity
- Missing goods receipt
- Duplicate invoice number
- Expired contract
- Missing approval

## 13.7 Drug Supplier Credit Monitoring

Monitor:

- Outstanding supplier credits
- Due date
- Overdue days
- Payment plan
- Disputed amounts
- Batch references
- Delivery references

Escalate urgent drug supplier credits that may affect service delivery.

## 13.8 Stock Issues

When issuing stock:

1. Select item.
2. Select location.
3. Select batch if batch-controlled.
4. Enter quantity.
5. Record destination:
    - Patient charge
    - Department
    - Internal transfer
    - Write-off
6. Save.

Do not issue expired stock unless following institutional disposal policy.

## 13.9 Batch and Expiry Monitoring

Regularly review:

- Batches expiring soon
- Negative stock balances
- Stock valuation
- Discrepancies between stock records and financial records
- Write-offs

Report expired or damaged stock according to policy.

## 13.10 Pharmacy Daily Reconciliation

For pharmacy operations:

1. Open pharmacy reconciliation.
2. Review:
    - Opening supplier credit
    - New supplier invoices
    - Supplier payments
    - Expected closing credit
    - Actual closing credit
3. Enter explanation for variance.
4. Submit for review.

---

# 14. Procurement Officer Usage Guide

## 14.1 Role of the Procurement Officer

The Procurement Officer manages purchasing and supplier invoice matching.

## 14.2 Main Responsibilities

- Create purchase requests
- Create purchase orders
- Track approvals
- Track goods and services received
- Match supplier invoices
- Monitor procurement commitments
- Identify price and quantity variances
- Support supplier statement reconciliation

## 14.3 Purchase Requests

To create a purchase request:

1. Open **Procurement** → **Purchase Requests**.
2. Enter:
    - Department
    - Requested by
    - Needed-by date
    - Description
    - Estimated amount
3. Save.
4. Submit for approval.

## 14.4 Purchase Orders

To create a purchase order:

1. Open approved purchase request if applicable.
2. Select supplier.
3. Enter order date and expected delivery date.
4. Enter line items.
5. Select budget line if required.
6. Save.
7. Submit for approval.

## 14.5 Three-Way Matching

Before approving a supplier invoice, match:

1. Purchase order
2. Goods or services receipt
3. Supplier invoice

Check:

- Quantity ordered
- Quantity received
- Quantity invoiced
- Unit price ordered
- Unit price invoiced
- Total amount
- Tax
- Delivery date
- Invoice date
- Due date

## 14.6 Handling Invoice Discrepancies

If an invoice does not match:

1. Mark the invoice as disputed if required.
2. Record the discrepancy.
3. Notify the supplier or requesting department.
4. Do not approve until resolved unless authorized.

## 14.7 Commitment Monitoring

Monitor open purchase orders and budget reservations.

Do not approve invoices that exceed available budget unless authorized.

---

# 15. Auditor Usage Guide

## 15.1 Role of the Auditor

The Auditor reviews financial activity without changing records.

This role is essential for internal control, compliance, and investigation.

## 15.2 Main Responsibilities

- Review transactions
- Review approvals
- Review audit logs
- Review reconciliations
- Review period close evidence
- Export reports
- Identify control weaknesses

## 15.3 Audit Log Review

Auditors can review:

- Login activity
- Transaction creation
- Transaction updates
- Status changes
- Approvals
- Rejections
- Cancellations
- Voids
- Payment allocations
- Reversals
- Write-offs
- Period changes
- User role changes
- Settings changes
- File uploads and deletions

## 15.4 Audit Evidence

When reviewing a transaction, the Auditor should check:

- Supporting documents
- Approval history
- Accounting entry
- Payment allocation
- Reconciliation reference
- Correction history
- Reversal history

## 15.5 Reporting Issues

If the Auditor finds an issue, they should document:

- Transaction reference
- Department
- Date
- Amount
- Nature of issue
- Evidence
- Recommended correction

The Auditor normally does not correct the transaction directly.

---

# 16. Viewer Usage Guide

## 16.1 Role of the Viewer

The Viewer has read-only access to permitted dashboards and reports.

This role is suitable for management users who need visibility but do not need to enter or approve data.

## 16.2 What a Viewer Can Do

- View dashboards
- View permitted reports
- Filter data
- Sort data
- Export permitted reports
- Print permitted reports

## 16.3 What a Viewer Cannot Do

- Create transactions
- Edit transactions
- Approve transactions
- Post transactions
- Cancel transactions
- Delete records
- Change settings
- Manage users

---

# 17. Common Workflow: Accounts Payable

This workflow is used by Department Finance Officers, Procurement Officers, Department Managers, and Central Accounting.

## Step 1: Create Bill or Supplier Invoice

Enter supplier invoice details and line items.

## Step 2: Attach Supporting Documents

Attach:

- Invoice
- Purchase order
- Goods receipt
- Delivery note
- Contract if required
- Approval memo if required

## Step 3: Submit for Approval

The preparer submits the bill.

## Step 4: Department Approval

The Department Manager reviews and approves or rejects.

## Step 5: Central Review

Central accounting validates coding and compliance.

## Step 6: Posting

Approved bills are posted to accounts payable.

## Step 7: Payment

When payment is made:

1. Select supplier.
2. Enter payment amount.
3. Allocate to invoices.
4. Post payment.
5. Update outstanding balance.

## Step 8: Reconciliation

Reconcile supplier balances and payment allocations.

---

# 18. Common Workflow: Accounts Receivable

This workflow is used by School Fees Officers, Hospital Billing Officers, Department Finance Officers, and Central Accounting.

## Step 1: Create Charge or Invoice

Create the receivable for:

- Student fees
- Patient services
- Insurance claims
- Government reimbursement
- Training services
- Other services

## Step 2: Approve if Required

Some receivables may require approval before posting.

## Step 3: Send or Notify Payer

Where applicable, mark the invoice as sent.

## Step 4: Record Payment

Record payment received from:

- Student
- Sponsor
- Patient
- Insurance provider
- Government
- Other payer

## Step 5: Allocate Payment

Allocate payment to outstanding receivables.

## Step 6: Issue Receipt

Generate official receipt.

## Step 7: Reconcile

Reconcile receipts, cashier sessions, and receivable balances.

---

# 19. Common Workflow: Payment Allocation

Payment allocation is one of the most sensitive financial activities.

## Rules

1. One payment may be allocated to one or multiple transactions.
2. Allocation cannot exceed payment amount.
3. Allocation cannot exceed transaction balance.
4. Fully paid transactions cannot receive additional allocation.
5. Cancelled or written-off transactions cannot receive normal payment.
6. Unallocated amounts must be monitored.
7. Reallocations must be approved where required.

## To Allocate a Payment

1. Open the payment or receipt.
2. Review available amount.
3. Select outstanding transactions.
4. Enter allocation amounts.
5. Confirm total allocation.
6. Save.
7. Post.

## If Allocation Is Wrong

Do not delete the payment.

Use:

- Reallocation
- Reversal
- Adjustment
- Refund where appropriate

Provide a reason and obtain approval if required.

---

# 20. Common Workflow: Department Submission

Departments must submit periodic reports to central accounting.

## Step 1: Complete Transactions

Ensure all bills, invoices, charges, receipts, and payments are entered.

## Step 2: Correct Returned Items

Resolve all returned transactions.

## Step 3: Reconcile Local Records

Confirm that:

- Opening balance + activity = closing balance
- Collections match cashier records
- Payments match supplier records
- No unexplained negative balances exist

## Step 4: Prepare Submission

Create submission for the period.

## Step 5: Department Manager Approval

Department Manager reviews and approves.

## Step 6: Central Accounting Review

Central accounting accepts, rejects, or returns the submission.

## Step 7: Posting or Consolidation

Accepted submissions are posted or consolidated into central accounting.

## Step 8: Period Close

After all departments are reviewed, central accounting closes the period.

---

# 21. Common Workflow: Period Close

Period close is controlled by central accounting.

## Step 1: Run Department Submissions

All departments submit their activity.

## Step 2: Review Submissions

Central accounting reviews and accepts or rejects.

## Step 3: Post Remaining Transactions

Post approved transactions.

## Step 4: Reconcile

Run reconciliations:

- Sub-ledger to general ledger
- Bank or treasury reconciliation
- Cashier reconciliation
- Payment allocation reconciliation
- Internal billing reconciliation
- Inventory valuation reconciliation

## Step 5: Run Financial Validations

Validate:

- Trial balance
- Balance sheet equation
- Statement of activity
- Unposted transactions
- Duplicate postings
- Negative balances

## Step 6: Soft Close

Soft close the period to stop normal transaction entry.

## Step 7: Final Adjustments

Only authorized central users may make late adjustments with documented reasons.

## Step 8: Hard Close

Hard close the period.

## Step 9: Lock

Lock the period for final immutability where required.

---

# 22. Common Workflow: Corrections and Reversals

Financial systems should not permanently delete records.

## When Correction Is Needed

Use the appropriate method:

| Situation | Correct Method |
|---|---|
| Wrong amount entered | Adjustment or reversal and replacement |
| Wrong department | Reclassification or correction entry |
| Wrong account code | Reclassification |
| Wrong payer or supplier | Correction with audit reason |
| Duplicate transaction | Cancel or reverse duplicate |
| Wrong payment allocation | Reallocate payment |
| Erroneous receipt | Reverse receipt or refund |
| Erroneous bill | Cancel or reverse bill |
| Wrong period | Period adjustment workflow if period allows |

## Rules for Reversals

1. Always provide a reason.
2. Always preserve the original record.
3. Always create a clear audit trail.
4. Always obtain approval where required.
5. Never edit a posted accounting entry directly.

---

# 23. Common Workflow: Attachments and Supporting Documents

Supporting documents are essential.

## Documents to Attach

Attach relevant documents to:

- Bills
- Invoices
- Student charge adjustments
- Patient charge adjustments
- Insurance claims
- Purchase orders
- Goods receipts
- Payments
- Receipts
- Waivers
- Scholarships
- Write-offs
- Reconciliations
- Period close evidence

## Attachment Rules

- Use clear file names.
- Upload only permitted file types.
- Do not upload executable files.
- Keep file sizes within configured limits.
- Ensure documents are readable.
- Do not attach confidential clinical details unless required for billing and authorized.

---

# 24. Using Dashboards Effectively

## 24.1 Institution Dashboard

Use for high-level monitoring:

- Total AP
- Total AR
- Student fee receivables
- Patient debt
- Insurance receivables
- Supplier credits
- Cash-flow forecast
- Overdue balances
- Department submission compliance

## 24.2 Department Dashboard

Use for operational monitoring:

- Department balances
- Collections
- Payments due
- Overdue items
- Returned transactions
- Pending approvals

## 24.3 Education Dashboard

Use for school performance:

- Fees billed
- Fees collected
- Outstanding fees
- Collection rate
- Scholarships and waivers
- Overdue students

## 24.4 Health Dashboard

Use for hospital performance:

- Patient charges
- Patient collections
- Insurance receivables
- Government receivables
- Rejected claims
- Patient debt aging

## 24.5 Supplier Dashboard

Use for procurement and pharmacy:

- Supplier credits
- Drug supplier balances
- Medical supply balances
- Overdue invoices
- Purchase commitments

---

# 25. Reporting Guide

## 25.1 Before Running a Report

Always select:

- Department or institution scope
- Date range or as-of date
- Currency where applicable
- Status filters
- Fund, project, or grant where applicable

## 25.2 Common Reports

### Accounts Payable Reports

- AP outstanding summary
- AP aging
- Supplier transaction history
- Supplier payment history
- Overdue supplier invoices
- Purchase order commitments
- Unmatched invoices

### Accounts Receivable Reports

- AR outstanding summary
- AR aging
- Customer transaction history
- Student fee outstanding
- Patient debt outstanding
- Insurance receivables
- Government receivables
- Collection performance

### Cash and Treasury Reports

- Cashbook
- Bank reconciliation
- Cashier session reconciliation
- Treasury balances
- Payment forecast

### Accounting Reports

- Trial balance
- General ledger
- Journal entries
- Financial statements
- Period movement report
- Reconciliation report

### Budget Reports

- Budget vs actual
- Fund utilization
- Grant utilization
- Budget reservations
- Budget adjustments

## 25.3 Exporting Reports

Use export options:

- CSV for data analysis
- XLSX for spreadsheets
- PDF for formal documents
- Print view where available

Before exporting, verify filters and user permissions.

---

# 26. Security and Confidentiality

## 26.1 User Responsibilities

- Do not share your password.
- Do not leave your session unattended.
- Do not access departments or records outside your responsibility.
- Do not export data unless authorized.
- Do not send confidential financial data to unauthorized recipients.

## 26.2 Patient and Student Data

Financial reports should use account numbers and summarized data where possible.

Avoid exposing unnecessary personal details in reports.

## 26.3 Audit Trail

All significant actions may be logged.

Actions logged may include:

- Login
- Logout
- Create
- Update
- Approve
- Reject
- Cancel
- Reverse
- Allocate
- Reallocate
- Close period
- Export
- File upload
- File deletion

---

# 27. Best Practices by Role

## 27.1 For Data Entry Users

- Enter transactions on the day they occur.
- Use official reference numbers.
- Check amounts before saving.
- Attach documents immediately.
- Do not leave unallocated receipts unexplained.

## 27.2 For Approvers

- Review supporting documents.
- Do not approve incomplete transactions.
- Do not approve transactions outside your authority.
- Reject with clear reasons.

## 27.3 For Central Accounting

- Reconcile before closing.
- Investigate unusual variances.
- Document corrections.
- Avoid reopening closed periods without authorization.

## 27.4 For Auditors

- Preserve evidence.
- Use filters and date ranges carefully.
- Document findings clearly.
- Do not modify financial data.

---

# 28. Common Errors and How to Avoid Them

## 28.1 Duplicate Invoice or Receipt

**Cause:** Entering the same reference twice.

**Prevention:** Search before creating. Use unique numbering.

## 28.2 Payment Over-Allocation

**Cause:** Allocating more than payment amount or transaction balance.

**Prevention:** Review available balance before allocating.

## 28.3 Wrong Department

**Cause:** User selected incorrect facility.

**Prevention:** Confirm current department context before entering data.

## 28.4 Wrong Period

**Cause:** Transaction dated outside reporting period.

**Prevention:** Check transaction date and accounting period.

## 28.5 Unexplained Cashier Variance

**Cause:** Missing receipt, reversal, refund, or wrong amount.

**Prevention:** Reconcile daily and investigate immediately.

## 28.6 Missing Supporting Document

**Cause:** Document not uploaded.

**Prevention:** Upload before submission.

## 28.7 Negative Balance

**Cause:** Incorrect reversal, refund, allocation, or data entry.

**Prevention:** Review balance before posting corrections.

## 28.8 Unposted Transaction

**Cause:** Validation error, closed period, or posting failure.

**Prevention:** Monitor posting status and error messages.

---

# 29. Frequently Asked Questions

## Q1: Can I delete a wrong transaction?

No. Financial transactions should not be permanently deleted. Use cancellation, reversal, adjustment, or write-off.

## Q2: What if I posted to the wrong department?

Contact central accounting or use the correction workflow. A reclassification or adjustment may be required.

## Q3: What if a payment was allocated to the wrong invoice?

Use reallocation or reversal. Do not delete the payment.

## Q4: Can I enter a transaction into a closed period?

Usually no. If the period is soft closed, authorized users may enter late adjustments. If locked, reopening requires special approval.

## Q5: What if my report totals do not match?

Check:

- Date range
- Department
- Status filters
- Currency
- Unposted transactions
- Reconciliation differences
- Manual adjustments

## Q6: What should I do if I cannot find a supplier invoice?

Search by:

- Supplier name
- Invoice number
- Date
- Amount
- Purchase order
- Status

If still missing, verify whether it was entered under another supplier or reference.

## Q7: Can a student have more than one sponsor?

Yes. A student may have multiple sponsors, and one sponsor may pay for multiple students.

## Q8: Can a patient payment be allocated across multiple charges?

Yes. Patient payments can usually be allocated across encounters and charges.

## Q9: What is a three-way match?

A three-way match compares:

1. Purchase order
2. Goods or services receipt
3. Supplier invoice

It helps prevent payment errors and fraud.

## Q10: What does “unallocated payment” mean?

It means the payment has been received but not yet applied to a specific invoice, charge, or account balance.

Unallocated payments should be reviewed regularly.

---

# 30. Quick Reference: Role-Based Daily Tasks

| Role | Daily Tasks |
|---|---|
| System Administrator | Check user issues, failed logins, system jobs |
| Central Accounting Manager | Review dashboards, approvals, exceptions |
| Central Accounting Officer | Review submissions, reconciliations, unposted items |
| Department Manager | Approve transactions, monitor balances |
| Department Finance Officer | Enter transactions, record payments, prepare submissions |
| School Fees Officer | Collect fees, issue receipts, reconcile daily collections |
| Hospital Billing Officer | Bill patients, submit claims, record payments |
| Pharmacy Officer | Receive stock, monitor supplier credits, reconcile pharmacy |
| Procurement Officer | Match invoices, track purchase orders, resolve disputes |
| Auditor | Review audit logs, reports, controls |
| Viewer | Review permitted dashboards and reports |

---

# 31. Quick Reference: Monthly Close Tasks

| Task | Responsible User |
|---|---|
| Complete departmental entries | Department Finance Officer |
| Approve departmental transactions | Department Manager |
| Submit department report | Department Finance Officer |
| Review department submissions | Central Accounting Officer |
| Approve or reject submissions | Central Accounting Manager |
| Reconcile sub-ledgers | Central Accounting Officer |
| Validate trial balance | Central Accounting Manager |
| Run financial statement validation | Central Accounting Manager |
| Soft close period | Central Accounting Manager |
| Hard close period | Central Accounting Manager |
| Lock period if required | Central Accounting Manager |

---

# 32. Glossary

| Term | Meaning |
|---|---|
| AP | Accounts Payable; money owed to suppliers |
| AR | Accounts Receivable; money owed to the institution |
| Bill | Supplier invoice requesting payment |
| Invoice | Request for payment from customer or payer |
| Allocation | Applying a payment to outstanding transactions |
| Aging | Classification of unpaid balances by due date |
| Reconciliation | Comparing two sets of records to confirm they agree |
| Posting | Recording a transaction in the accounting system |
| Period Close | Process of finalizing an accounting period |
| Soft Close | Period closed to normal users but limited corrections allowed |
| Hard Close | Period closed more strictly |
| Locked Period | Period that cannot be changed without exceptional approval |
| Write-off | Removal of an uncollectible balance with approval |
| Credit Note | Adjustment reducing an amount owed |
| Debit Note | Adjustment increasing an amount owed |
| Sub-ledger | Detailed module such as student fees, patient billing, or supplier credits |
| General Ledger | Central accounting record |
| Fund | Restricted or unrestricted funding source |
| Grant | Donor or program funding with conditions |
| Cost Center | Unit used to accumulate costs |
| Three-way Match | Matching purchase order, receipt, and invoice |

---

# 33. Support and Escalation

If you experience an issue:

1. Note the screen name.
2. Note the transaction reference.
3. Note the date and time.
4. Take a screenshot if permitted.
5. Describe what you were trying to do.
6. Contact your supervisor or System Administrator.

For financial discrepancies, escalate to:

1. Department Manager
2. Central Accounting Officer
3. Central Accounting Manager
4. Internal Audit where required

Do not attempt to correct financial discrepancies by deleting records.

---

# 34. Final User Principles

Every user should follow these principles:

1. **Accuracy**: Enter data carefully and verify amounts.
2. **Completeness**: Attach required documents and explanations.
3. **Timeliness**: Record transactions when they occur.
4. **Authorization**: Do not act beyond your role.
5. **Traceability**: Preserve audit trails.
6. **Confidentiality**: Protect sensitive institutional, student, patient, and supplier data.
7. **Reconciliation**: Investigate differences promptly.
8. **Accountability**: Every correction must have a reason and approval where required.