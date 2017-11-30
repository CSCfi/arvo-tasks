CREATE TABLE uraseuranta.kyselykerta(
  uraseuranta_id INTEGER NOT NULL REFERENCES uraseuranta.uraseuranta(id),
  kyselykertaid INTEGER NOT NULL,
  oppilaitoskoodi TEXT NOT NULL,
  uraseuranta_tyyppi TEXT NOT NULL,
  vastaajia INTEGER NOT NULL
)