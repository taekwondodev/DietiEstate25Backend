--
-- Dati di test DietiEstates25
--
-- Utenti:
--   uid-admin-001   → Admin,              agenzia 1
--   uid-gestore-001 → Gestore,            agenzia 1
--   uid-agente-001  → AgenteImmobiliare,  agenzia 1
--   uid-agente-002  → AgenteImmobiliare,  agenzia 2
--   uid-cliente-001 → Cliente
--   uid-cliente-002 → Cliente
--
-- Password: tutte le password sono BCrypt di "Test1234!"
-- Hash: $2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.
-- (usare questo hash nei test Mockito che verificano BCrypt, non il DB diretto)
--

-- ---------------------------------------------------------------------------
-- agenzia
-- ---------------------------------------------------------------------------

INSERT INTO public.agenzia (idagenzia) VALUES (1), (2);
SELECT pg_catalog.setval('public.agenzia_idagenzia_seq', 2, true);

-- ---------------------------------------------------------------------------
-- utenti  (un rappresentante per ogni ruolo)
-- ---------------------------------------------------------------------------

INSERT INTO public.utenti (uid, email, password, role) VALUES
    ('uid-admin-001',   'admin@test.it',    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Admin'),
    ('uid-gestore-001', 'gestore@test.it',  '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Gestore'),
    ('uid-agente-001',  'agente1@test.it',  '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'AgenteImmobiliare'),
    ('uid-agente-002',  'agente2@test.it',  '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'AgenteImmobiliare'),
    ('uid-cliente-001', 'cliente1@test.it', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Cliente'),
    ('uid-cliente-002', 'cliente2@test.it', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Cliente');

-- ---------------------------------------------------------------------------
-- utenteagenzia
-- ---------------------------------------------------------------------------

INSERT INTO public.utenteagenzia (uid, idagenzia) VALUES
    ('uid-admin-001',   1),
    ('uid-gestore-001', 1),
    ('uid-agente-001',  1),
    ('uid-agente-002',  2);

-- ---------------------------------------------------------------------------
-- immobile  (uno per comune, utili per testare /immobile/cerca)
-- ---------------------------------------------------------------------------

INSERT INTO public.immobile
    (urlfoto, descrizione, prezzo, dimensione, nbagni, nstanze, tipologia,
     latitudine, longitudine, indirizzo, comune, piano, hasascensore, hasbalcone, idagente)
VALUES
    ('https://test.it/foto1.jpg', 'Appartamento luminoso con vista',
     250000.00, 80, 1, 3, 'Appartamento',
     41.9028, 12.4964, 'Via Roma 1', 'Roma', 2, true, true, 'uid-agente-001'),

    ('https://test.it/foto2.jpg', 'Villetta con giardino privato',
     450000.00, 150, 2, 5, 'Villa',
     45.4642, 9.1900, 'Via Milano 10', 'Milano', 0, false, false, 'uid-agente-001'),

    ('https://test.it/foto3.jpg', 'Monolocale in centro storico',
     120000.00, 35, 1, 1, 'Monolocale',
     40.8518, 14.2681, 'Via Napoli 5', 'Napoli', 3, true, false, 'uid-gestore-001'),

    ('https://test.it/foto4.jpg', 'Bilocale moderno',
     180000.00, 60, 1, 2, 'Appartamento',
     45.0703, 7.6869, 'Via Torino 8', 'Torino', 1, true, false, 'uid-agente-002');

SELECT pg_catalog.setval('public.immobile_idimmobile_seq', 4, true);

-- ---------------------------------------------------------------------------
-- offerta  (IN_SOSPESO e ACCETTATA per testare state machine)
-- ---------------------------------------------------------------------------

INSERT INTO public.offerta (importo, stato, idcliente, idimmobile) VALUES
    (240000.00, 'In Sospeso', 'uid-cliente-001', 1),
    (430000.00, 'Accettata',  'uid-cliente-002', 2),
    (115000.00, 'Rifiutata',  'uid-cliente-001', 3);

SELECT pg_catalog.setval('public.offerta_idofferta_seq', 3, true);

-- ---------------------------------------------------------------------------
-- visita  (IN_SOSPESO e CONFERMATA per testare state machine)
-- ---------------------------------------------------------------------------

INSERT INTO public.visita (data, orario, stato, idcliente, idimmobile) VALUES
    ('2026-05-10', '10:00:00', 'Confermata',  'uid-cliente-001', 1),
    ('2026-05-12', '15:30:00', 'In Sospeso',  'uid-cliente-002', 3),
    ('2026-05-15', '09:00:00', 'Rifiutata',   'uid-cliente-001', 2);

SELECT pg_catalog.setval('public.visita_idvisita_seq', 3, true);
