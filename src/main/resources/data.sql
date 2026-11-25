-- Mock data for practice, dated relative to the day the app first starts so the
-- dashboard widgets ("Who's off", attendance, payslips) have something current to show.
--
-- Runs after schema.sql on every start, but each insert only happens while the
-- 'current-activity-v1' marker is missing. To insert a fresh set later, delete the
-- rows you don't want and run: DELETE FROM mock_data_seed WHERE name = 'current-activity-v1';

-- 1. Leave requests: some approved around today, some pending for the approvals queue.
INSERT INTO leaverequest (leaveid, employeeid, leavetype, fromdate, todate, reason, status)
SELECT nextval('leaverequest_seq'), seed.employeeid, seed.leavetype,
       CURRENT_DATE + seed.fromoffset, CURRENT_DATE + seed.tooffset, seed.reason, seed.status
FROM (VALUES
    -- Approved, covering today or the next two weeks
    (5,  'Sick Leave',        -1,  1, 'Fever, doctor advised rest',       'Approved'),
    (8,  'Casual Leave',       0,  0, 'Personal errand',                  'Approved'),
    (2,  'Earned Leave',       2,  6, 'Family trip to Ooty',              'Approved'),
    (4,  'Work From Home',     3,  3, 'Home internet installation',       'Approved'),
    (7,  'Casual Leave',       5,  5, 'Bank work',                        'Approved'),
    (10, 'Earned Leave',       7, 11, 'Sister''s wedding',                'Approved'),
    (12, 'Compensatory Off',   9,  9, 'Worked on the weekend release',    'Approved'),
    (11, 'Paternity Leave',   12, 25, 'Birth of first child',             'Approved'),
    -- Pending, waiting in the approvals queue
    (3,  'Earned Leave',      14, 18, 'Year-end vacation',                'Pending'),
    (6,  'Casual Leave',       4,  4, 'Parent-teacher meeting',           'Pending'),
    (9,  'Sick Leave',         1,  2, 'Dental surgery',                   'Pending'),
    (13, 'Work From Home',     6,  8, 'Recovering from a minor injury',   'Pending'),
    (14, 'Earned Leave',      20, 30, 'Christmas holidays with family',   'Pending'),
    (15, 'Compensatory Off',   8,  8, 'Covered the on-call shift',        'Pending'),
    (1,  'Earned Leave',      35, 39, 'New year break',                   'Pending'),
    -- History from earlier this year
    (1,  'Sick Leave',      -150, -148, 'Viral fever',                    'Approved'),
    (1,  'Casual Leave',     -60,  -60, 'Moving house',                   'Approved'),
    (16, 'Earned Leave',     -30,  -26, 'Diwali at home',                 'Approved'),
    (17, 'Casual Leave',     -10,  -10, 'Late request, team short-staffed','Rejected')
) AS seed(employeeid, leavetype, fromoffset, tooffset, reason, status)
WHERE NOT EXISTS (SELECT 1 FROM mock_data_seed WHERE name = 'current-activity-v1');

-- 2. Attendance for every employee on each weekday of the last three weeks.
INSERT INTO attendance (attendanceid, employeeid, attendancedate, checkin, checkout, status)
SELECT (SELECT COALESCE(MAX(attendanceid), 0) FROM attendance)
           + ROW_NUMBER() OVER (ORDER BY d.day, e.employeeid),
       e.employeeid,
       d.day,
       CASE WHEN s.status IN ('Present', 'Work From Home')
            THEN TIME '08:45' + ((e.employeeid * 7 + EXTRACT(DOY FROM d.day)::int) % 50) * INTERVAL '1 minute' END,
       CASE WHEN s.status IN ('Present', 'Work From Home')
            THEN TIME '17:45' + ((e.employeeid * 11 + EXTRACT(DOY FROM d.day)::int) % 60) * INTERVAL '1 minute' END,
       s.status
FROM employee e
CROSS JOIN (SELECT generate_series(CURRENT_DATE - 21, CURRENT_DATE - 1, INTERVAL '1 day')::date AS day) d
CROSS JOIN LATERAL (
    SELECT CASE
               WHEN EXISTS (SELECT 1 FROM leaverequest l
                            WHERE l.employeeid = e.employeeid AND l.status = 'Approved'
                              AND d.day BETWEEN l.fromdate AND l.todate) THEN 'On Leave'
               WHEN (e.employeeid + EXTRACT(DOY FROM d.day)::int) % 13 = 0 THEN 'Absent'
               WHEN (e.employeeid + EXTRACT(DOY FROM d.day)::int) % 5 = 0 THEN 'Work From Home'
               ELSE 'Present'
           END AS status
) s
WHERE EXTRACT(ISODOW FROM d.day) < 6
  AND NOT EXISTS (SELECT 1 FROM mock_data_seed WHERE name = 'current-activity-v1');

-- 3. Payslips for the last six full months, based on each employee's monthly salary.
INSERT INTO payroll (payrollid, employeeid, month, basicsalary, bonus, deduction, netsalary)
SELECT (SELECT COALESCE(MAX(payrollid), 0) FROM payroll) + ROW_NUMBER() OVER (ORDER BY m.month, e.employeeid),
       e.employeeid,
       to_char(m.month, 'FMMonth YYYY'),
       e.salary,
       ROUND(e.salary * ((e.employeeid + EXTRACT(MONTH FROM m.month)::int) % 4) * 0.025, 2),
       ROUND(e.salary * 0.12, 2),
       e.salary
           + ROUND(e.salary * ((e.employeeid + EXTRACT(MONTH FROM m.month)::int) % 4) * 0.025, 2)
           - ROUND(e.salary * 0.12, 2)
FROM employee e
CROSS JOIN (SELECT generate_series(date_trunc('month', CURRENT_DATE) - INTERVAL '6 months',
                                   date_trunc('month', CURRENT_DATE) - INTERVAL '1 month',
                                   INTERVAL '1 month')::date AS month) m
WHERE NOT EXISTS (SELECT 1 FROM mock_data_seed WHERE name = 'current-activity-v1');

-- 4. A mid-year performance review for every employee.
INSERT INTO performancereview (reviewid, employeeid, reviewdate, rating, comments)
SELECT (SELECT COALESCE(MAX(reviewid), 0) FROM performancereview) + ROW_NUMBER() OVER (ORDER BY e.employeeid),
       e.employeeid,
       CURRENT_DATE - 120 + (e.employeeid % 10),
       r.rating,
       (ARRAY['Needs a clear improvement plan for the next cycle.',
              'Meets some expectations; focus on delivery timelines.',
              'Solid, dependable contributor who meets expectations.',
              'Strong ownership and quality; a go-to person on the team.',
              'Exceptional impact this half; ready for bigger responsibilities.'])[r.rating]
FROM employee e
CROSS JOIN LATERAL (SELECT CASE WHEN e.employeeid % 9 = 0 THEN 2
                                WHEN e.employeeid % 4 = 0 THEN 5
                                WHEN e.employeeid % 3 = 0 THEN 3
                                ELSE 4 END AS rating) r
WHERE NOT EXISTS (SELECT 1 FROM mock_data_seed WHERE name = 'current-activity-v1');

-- 5. More announcements and events, including one event that has already happened.
INSERT INTO announcement (title, summary, author, postedat)
SELECT * FROM (VALUES
    ('Open enrolment for health insurance',
     'Add or update dependants on your group health policy before the end of the month.',
     'HR Team', CURRENT_TIMESTAMP - INTERVAL '5 days'),
    ('New office in Coimbatore',
     'Our Coimbatore office opens next month with seats for 120 people. Ask your manager about moving.',
     'Facilities', CURRENT_TIMESTAMP - INTERVAL '8 days'),
    ('Security reminder',
     'Never share your password. IT will never ask for it over email or chat.',
     'IT Security', CURRENT_TIMESTAMP - INTERVAL '12 days')
) AS seed(title, summary, author, postedat)
WHERE NOT EXISTS (SELECT 1 FROM mock_data_seed WHERE name = 'current-activity-v1');

INSERT INTO company_event (title, description, eventdate, location)
SELECT * FROM (VALUES
    ('Diwali celebration', 'Lunch, rangoli contest and games for all teams.', CURRENT_DATE - 5, 'Chennai'),
    ('Blood donation camp', 'Organised with the city blood bank. Walk in any time.', CURRENT_DATE + 6, 'Pune'),
    ('Year-end party', 'Dinner, awards and music to close the year.', CURRENT_DATE + 30, 'Mumbai')
) AS seed(title, description, eventdate, location)
WHERE NOT EXISTS (SELECT 1 FROM mock_data_seed WHERE name = 'current-activity-v1');

-- Keep the leave sequence ahead of the rows above, then record that this set is in.
SELECT setval('leaverequest_seq', (SELECT COALESCE(MAX(leaveid), 0) + 1 FROM leaverequest), false);

INSERT INTO mock_data_seed (name) VALUES ('current-activity-v1') ON CONFLICT (name) DO NOTHING;


-- =================================================================================
-- Mock data for the Documents, Tasks, Library and Help & support pages.
-- Inserted once, guarded by the 'portal-modules-v1' marker.
-- =================================================================================

-- 1. Company-wide documents (no owner).
INSERT INTO document (title, category, description, content, uploadedby, updatedat)
SELECT seed.title, seed.category, seed.description, seed.content, 'HR Team', CURRENT_TIMESTAMP - seed.age
FROM (VALUES
    ('Leave policy 2026', 'Policies', 'Leave types, yearly entitlement and how to apply.',
     E'LEAVE POLICY 2026\n\n1. Entitlement\n- Earned leave: 18 days a year, credited monthly (1.5 days).\n- Casual leave: 8 days a year.\n- Sick leave: 10 days a year. A medical certificate is needed for more than 2 consecutive days.\n- Maternity leave: 26 weeks. Paternity leave: 2 weeks.\n- Compensatory off: 1 day for each weekend or holiday worked, used within 60 days.\n\n2. Applying\nApply through My services > Time off at least 3 working days before planned leave. Emergency and sick leave can be applied on the day.\n\n3. Carry forward\nUp to 30 days of earned leave carry forward to the next year. Casual and sick leave lapse on 31 December.\n\n4. Approval\nYour reporting manager approves or rejects requests within 2 working days.',
     INTERVAL '40 days'),
    ('Code of conduct', 'Policies', 'How we work with each other, customers and partners.',
     E'CODE OF CONDUCT\n\nRespect: treat every colleague, customer and partner with dignity. Harassment and discrimination of any kind are not tolerated.\n\nIntegrity: be honest in your work, records and expense claims. Declare any conflict of interest to HR.\n\nConfidentiality: protect company, customer and employee information. Share it only with people who need it for their work.\n\nSpeaking up: report concerns to your manager, HR or the confidential ethics line. Nobody will face retaliation for raising a concern in good faith.',
     INTERVAL '120 days'),
    ('Information security policy', 'IT', 'Passwords, devices, email and data handling rules.',
     E'INFORMATION SECURITY POLICY\n\n- Use a unique password of at least 12 characters. Never share it; IT will never ask for it.\n- Lock your screen whenever you leave your desk (Windows + L).\n- Report lost or stolen laptops and phones to IT within 1 hour.\n- Do not install unapproved software or connect personal USB drives to company laptops.\n- Treat unexpected attachments and links as suspicious. Forward phishing emails to security@company.com.\n- Customer data must stay in approved systems and must never be copied to personal accounts.',
     INTERVAL '25 days'),
    ('Travel and expense policy', 'Finance', 'Booking travel and claiming expenses.',
     E'TRAVEL AND EXPENSE POLICY\n\nBooking: book flights and hotels through the travel desk at least 7 days ahead.\n\nDaily allowance: Rs 1,500 within India for meals and local travel.\n\nClaims: submit expenses with receipts within 30 days of returning. Claims are paid with the next salary.\n\nNot covered: alcohol, personal entertainment, fines and upgrades you chose yourself.',
     INTERVAL '75 days'),
    ('Work from home guidelines', 'Policies', 'Eligibility and expectations for remote days.',
     E'WORK FROM HOME GUIDELINES\n\n- Up to 2 work-from-home days a week, agreed with your manager.\n- Apply in My services > Time off with the type "Work From Home".\n- Be reachable on chat and phone between 10:00 and 17:00.\n- Use the company VPN for all work systems.\n- Internet reimbursement: up to Rs 1,000 a month.',
     INTERVAL '15 days'),
    ('Holiday calendar 2027', 'General', 'Company holidays for next year.',
     E'HOLIDAY CALENDAR 2027\n\n26 Jan - Republic Day\n14 Apr - Tamil New Year / Ambedkar Jayanti\n1 May - May Day\n15 Aug - Independence Day\n2 Oct - Gandhi Jayanti\n29 Oct - Diwali\n25 Dec - Christmas\n\nPlus 2 floating holidays of your choice, applied as casual leave.',
     INTERVAL '3 days'),
    ('Employee handbook', 'General', 'Everything a new joiner needs in the first month.',
     E'EMPLOYEE HANDBOOK\n\nWelcome to the company!\n\nWorking hours: 9:30 to 18:30, Monday to Friday, with a one-hour lunch break.\nPayday: the last working day of each month. Payslips appear in My services > Payslips.\nProbation: 6 months, followed by a confirmation review with your manager.\nLearning: borrow books and courses from the Library page.\nHelp: raise a ticket on the Help & support page for IT, HR, payroll or facilities questions.',
     INTERVAL '200 days')
) AS seed(title, category, description, content, age)
WHERE NOT EXISTS (SELECT 1 FROM mock_data_seed WHERE name = 'portal-modules-v1');

-- 2. Personal documents for every employee: an offer letter and a tax statement.
INSERT INTO document (title, category, description, content, ownerid, uploadedby, updatedat)
SELECT 'Offer letter', 'Personal', 'Your offer of employment.',
       'OFFER OF EMPLOYMENT' || E'\n\n' ||
       'Dear ' || e.firstname || ' ' || e.lastname || ',' || E'\n\n' ||
       'We are pleased to offer you the position of ' || COALESCE(d.designationname, 'Employee') ||
       ' in ' || COALESCE(dep.departmentname, 'our company') ||
       COALESCE(' (' || dep.location || ')', '') || ', starting on ' || to_char(e.hiredate, 'FMDD FMMonth YYYY') || '.' || E'\n\n' ||
       'Your monthly gross salary will be Rs ' || to_char(e.salary, 'FM99,99,999.00') || '.' || E'\n\n' ||
       'Welcome aboard!' || E'\n' || 'HR Team',
       e.employeeid, 'HR Team', e.hiredate::timestamp
FROM employee e
LEFT JOIN designation d ON d.designationid = e.designationid
LEFT JOIN department dep ON dep.departmentid = e.departmentid
WHERE NOT EXISTS (SELECT 1 FROM mock_data_seed WHERE name = 'portal-modules-v1');

INSERT INTO document (title, category, description, content, ownerid, uploadedby, updatedat)
SELECT 'Tax statement FY 2025-26', 'Personal', 'Annual salary and tax summary (Form 16 style).',
       'ANNUAL TAX STATEMENT - FY 2025-26' || E'\n\n' ||
       'Employee: ' || e.firstname || ' ' || e.lastname || ' (#' || e.employeeid || ')' || E'\n' ||
       'Gross salary: Rs ' || to_char(e.salary * 12, 'FM99,99,99,999.00') || E'\n' ||
       'Standard deduction: Rs 75,000.00' || E'\n' ||
       'Tax deducted at source: Rs ' || to_char(ROUND(e.salary * 12 * 0.08, 2), 'FM99,99,99,999.00') || E'\n\n' ||
       'This is a system-generated statement for practice purposes.',
       e.employeeid, 'Payroll Team', TIMESTAMP '2026-06-15 10:00'
FROM employee e
WHERE NOT EXISTS (SELECT 1 FROM mock_data_seed WHERE name = 'portal-modules-v1');

-- 3. Tasks: three per employee, assigned by employee 3 (the demo manager) or themselves.
INSERT INTO task (title, description, assigneeid, createdbyid, priority, status, duedate, createdat, completedat)
SELECT t.title, t.description, e.employeeid,
       CASE WHEN n = 3 OR e.employeeid = 3 THEN e.employeeid ELSE 3 END,
       (ARRAY['HIGH', 'MEDIUM', 'LOW'])[1 + (e.employeeid + n) % 3],
       s.status,
       CURRENT_DATE + ((e.employeeid * 3 + n * 5) % 24) - 6,
       CURRENT_TIMESTAMP - ((e.employeeid + n) % 20 + 5) * INTERVAL '1 day',
       CASE WHEN s.status = 'DONE' THEN CURRENT_TIMESTAMP - ((e.employeeid + n) % 4) * INTERVAL '1 day' END
FROM employee e
CROSS JOIN generate_series(1, 3) AS n
CROSS JOIN LATERAL (
    SELECT (ARRAY['Update project status report', 'Review pull requests from the team',
                  'Complete security awareness training', 'Prepare sprint demo',
                  'Fix reported production bug', 'Write API documentation',
                  'Plan next quarter goals', 'Submit pending expense claims',
                  'Onboard new team member', 'Update test cases for release'])[1 + (e.employeeid * 3 + n) % 10] AS title,
           'Created as practice data. Update the status as you make progress.' AS description
) t
CROSS JOIN LATERAL (
    SELECT (ARRAY['TODO', 'IN_PROGRESS', 'DONE', 'TODO'])[1 + (e.employeeid + n * 2) % 4] AS status
) s
WHERE NOT EXISTS (SELECT 1 FROM mock_data_seed WHERE name = 'portal-modules-v1');

-- 4. Library catalogue.
INSERT INTO library_item (title, author, category, format, description, totalcopies)
SELECT * FROM (VALUES
    ('Clean Code', 'Robert C. Martin', 'Software engineering', 'BOOK', 'Writing readable, maintainable code.', 3),
    ('The Pragmatic Programmer', 'David Thomas, Andrew Hunt', 'Software engineering', 'BOOK', 'Habits and practices of effective developers.', 2),
    ('Designing Data-Intensive Applications', 'Martin Kleppmann', 'Software engineering', 'BOOK', 'Databases, streams and distributed systems explained.', 2),
    ('Refactoring', 'Martin Fowler', 'Software engineering', 'EBOOK', 'Improving the design of existing code.', 5),
    ('Spring Boot in Practice', 'Somnath Musib', 'Java', 'BOOK', 'Hands-on recipes for Spring Boot applications.', 2),
    ('Effective Java', 'Joshua Bloch', 'Java', 'BOOK', 'Best practices for the Java platform.', 3),
    ('Angular Fundamentals', 'Internal Academy', 'Frontend', 'COURSE', '6-week guided course: components, signals, routing and forms.', 20),
    ('PostgreSQL for Developers', 'Internal Academy', 'Databases', 'COURSE', 'Indexes, query plans and transactions in practice.', 15),
    ('SQL Performance Explained', 'Markus Winand', 'Databases', 'EBOOK', 'How indexes really work.', 4),
    ('Cloud Fundamentals (AWS)', 'Internal Academy', 'Cloud', 'VIDEO', '8 hours of video lessons with labs.', 25),
    ('Kubernetes Up and Running', 'Brendan Burns et al.', 'Cloud', 'BOOK', 'Deploying and scaling containerised apps.', 1),
    ('Atomic Habits', 'James Clear', 'Personal growth', 'BOOK', 'Small changes, remarkable results.', 2),
    ('Deep Work', 'Cal Newport', 'Personal growth', 'BOOK', 'Focused success in a distracted world.', 2),
    ('Crucial Conversations', 'Kerry Patterson et al.', 'Leadership', 'BOOK', 'Tools for talking when stakes are high.', 1),
    ('The Manager''s Path', 'Camille Fournier', 'Leadership', 'BOOK', 'A guide for tech leaders navigating growth.', 2),
    ('Business English Communication', 'Internal Academy', 'Communication', 'VIDEO', 'Emails, meetings and presentations.', 30),
    ('Data Science Foundations', 'Internal Academy', 'Data', 'COURSE', 'Statistics, Python and visualisation basics.', 15),
    ('Storytelling with Data', 'Cole Nussbaumer Knaflic', 'Data', 'BOOK', 'Turning data into clear charts.', 1)
) AS seed(title, author, category, format, description, totalcopies)
WHERE NOT EXISTS (SELECT 1 FROM mock_data_seed WHERE name = 'portal-modules-v1');

-- 5. Loans: some active, a few overdue, some already returned.
INSERT INTO library_loan (itemid, employeeid, borrowedon, dueon, returnedon)
SELECT i.itemid, seed.employeeid, CURRENT_DATE + seed.borrowoffset, CURRENT_DATE + seed.borrowoffset + 14,
       CASE WHEN seed.returned THEN CURRENT_DATE + seed.borrowoffset + 10 END
FROM (VALUES
    ('Clean Code', 1, -5, false),
    ('Angular Fundamentals', 1, -20, false),
    ('Effective Java', 1, -60, true),
    ('Kubernetes Up and Running', 2, -18, false),
    ('Designing Data-Intensive Applications', 3, -3, false),
    ('Crucial Conversations', 4, -16, false),
    ('Clean Code', 5, -9, false),
    ('Storytelling with Data', 6, -2, false),
    ('Atomic Habits', 7, -11, false),
    ('Spring Boot in Practice', 8, -7, false),
    ('Spring Boot in Practice', 9, -1, false),
    ('The Pragmatic Programmer', 10, -30, true),
    ('Deep Work', 11, -25, true),
    ('The Manager''s Path', 12, -4, false),
    ('Clean Code', 13, -40, true)
) AS seed(title, employeeid, borrowoffset, returned)
JOIN library_item i ON i.title = seed.title
JOIN employee e ON e.employeeid = seed.employeeid
WHERE NOT EXISTS (SELECT 1 FROM mock_data_seed WHERE name = 'portal-modules-v1');

-- 6. Frequently asked questions.
INSERT INTO faq (category, question, answer, sortorder)
SELECT * FROM (VALUES
    ('Leave', 'How do I apply for leave?', 'Go to My services > Time off, choose the leave type and dates, and submit. Your manager is notified and the request shows as Pending until it is decided.', 1),
    ('Leave', 'Can I cancel a leave request?', 'Yes, while it is still Pending. Open My services > Time off and choose Withdraw next to the request. Approved leave must be cancelled by your manager.', 2),
    ('Leave', 'How many days of earned leave do I get?', '18 days a year, credited at 1.5 days a month. See the Leave policy in Documents for the full rules.', 3),
    ('Payroll', 'When is salary paid?', 'On the last working day of each month. Your payslip appears in My services > Payslips the same day.', 1),
    ('Payroll', 'Where can I find my tax statement?', 'Open Documents and look under Personal for "Tax statement FY 2025-26". You can download it as a text file.', 2),
    ('Payroll', 'My payslip looks wrong. What should I do?', 'Raise a ticket on this page with the category Payroll and mention the month and the amount you expected.', 3),
    ('IT', 'How do I change my password?', 'Click your avatar in the top-right corner and choose Change password. Changing it signs you out on every other device.', 1),
    ('IT', 'My account is locked. What now?', 'Accounts lock for 15 minutes after 5 wrong passwords. Wait and try again, or ask an administrator to unlock it from the User accounts page.', 2),
    ('IT', 'How do I get software installed?', 'Raise an IT ticket with the software name and why you need it. Approved software is installed within 2 working days.', 3),
    ('General', 'How do I update my phone number or address?', 'Ask HR by raising a ticket with the category HR. HR can update your profile from the Directory.', 1),
    ('General', 'How do I borrow a book or course?', 'Open the Library page and click Borrow. Books are due back in 14 days and you can hold up to 3 items at once.', 2),
    ('Facilities', 'Who do I contact about my desk, chair or access card?', 'Raise a ticket with the category Facilities. Urgent issues such as a lost access card are handled the same day.', 1)
) AS seed(category, question, answer, sortorder)
WHERE NOT EXISTS (SELECT 1 FROM mock_data_seed WHERE name = 'portal-modules-v1');

-- 7. Support tickets in different states.
INSERT INTO support_ticket (employeeid, category, subject, description, priority, status, resolution, createdat, updatedat)
SELECT seed.employeeid, seed.category, seed.subject, seed.description, seed.priority, seed.status, seed.resolution,
       CURRENT_TIMESTAMP - seed.age, CURRENT_TIMESTAMP - seed.age / 2
FROM (VALUES
    (1, 'IT', 'VPN disconnects every few minutes', 'Since yesterday the VPN drops every 5-10 minutes when working from home.', 'HIGH', 'IN_PROGRESS', NULL, INTERVAL '2 days'),
    (1, 'PAYROLL', 'Internet reimbursement missing in October payslip', 'I submitted the claim on 3 October but it is not in the October payslip.', 'MEDIUM', 'RESOLVED', 'The claim was approved late and will be paid with the November salary.', INTERVAL '20 days'),
    (2, 'FACILITIES', 'Air conditioning not working in meeting room 4B', 'The room gets very warm in the afternoon.', 'LOW', 'OPEN', NULL, INTERVAL '1 day'),
    (4, 'IT', 'Request to install Docker Desktop', 'Needed for local development of the new microservice.', 'MEDIUM', 'OPEN', NULL, INTERVAL '3 days'),
    (5, 'HR', 'Update emergency contact', 'Please change my emergency contact to my spouse.', 'LOW', 'RESOLVED', 'Updated in your HR record.', INTERVAL '15 days'),
    (6, 'IT', 'Laptop battery drains quickly', 'The battery lasts under 2 hours on a full charge.', 'MEDIUM', 'IN_PROGRESS', NULL, INTERVAL '6 days'),
    (7, 'PAYROLL', 'Clarification on tax deduction', 'TDS in September is higher than in August.', 'MEDIUM', 'OPEN', NULL, INTERVAL '4 days'),
    (8, 'FACILITIES', 'Lost access card', 'I lost my access card this morning and need a temporary one.', 'HIGH', 'RESOLVED', 'Temporary card issued; the old card has been deactivated.', INTERVAL '9 days'),
    (9, 'OTHER', 'Parking slot request', 'Requesting a two-wheeler parking slot from next month.', 'LOW', 'CLOSED', 'Slot B-14 allocated.', INTERVAL '30 days'),
    (10, 'HR', 'Experience letter for visa application', 'Need an experience letter addressed to the embassy.', 'MEDIUM', 'IN_PROGRESS', NULL, INTERVAL '5 days'),
    (12, 'IT', 'Cannot access the shared drive', 'Getting "access denied" on the Finance shared drive.', 'HIGH', 'OPEN', NULL, INTERVAL '12 hours')
) AS seed(employeeid, category, subject, description, priority, status, resolution, age)
JOIN employee e ON e.employeeid = seed.employeeid
WHERE NOT EXISTS (SELECT 1 FROM mock_data_seed WHERE name = 'portal-modules-v1');

INSERT INTO mock_data_seed (name) VALUES ('portal-modules-v1') ON CONFLICT (name) DO NOTHING;
