-- Ligix - suporte para o painel do docente.
-- Executar no Supabase Dashboard > SQL Editor antes de testar feedback do docente.

create table if not exists public.feedback_atividade (
    idfeedback uuid primary key default gen_random_uuid(),
    idatividade uuid not null references public.atividade(idatividade) on delete cascade,
    idestagio uuid not null references public.estagio(idestagio) on delete cascade,
    iddocente uuid not null references public.docente(idutilizador) on delete cascade,
    comentario text not null,
    created_at timestamptz not null default now()
);

create index if not exists feedback_atividade_estagio_idx
on public.feedback_atividade (idestagio);

create index if not exists feedback_atividade_atividade_idx
on public.feedback_atividade (idatividade);

alter table public.feedback_atividade enable row level security;

grant select, insert, update, delete on public.feedback_atividade to authenticated;

drop policy if exists "feedback_atividade_select_participantes" on public.feedback_atividade;
create policy "feedback_atividade_select_participantes"
on public.feedback_atividade for select
to authenticated
using (public.can_access_estagio(idestagio));

drop policy if exists "feedback_atividade_insert_docente" on public.feedback_atividade;
create policy "feedback_atividade_insert_docente"
on public.feedback_atividade for insert
to authenticated
with check (
    iddocente = auth.uid()
    and public.can_access_estagio(idestagio)
);

drop policy if exists "feedback_atividade_update_own_docente" on public.feedback_atividade;
create policy "feedback_atividade_update_own_docente"
on public.feedback_atividade for update
to authenticated
using (iddocente = auth.uid())
with check (
    iddocente = auth.uid()
    and public.can_access_estagio(idestagio)
);

drop policy if exists "docente_update_own" on public.docente;
create policy "docente_update_own"
on public.docente for update
to authenticated
using (idutilizador = auth.uid())
with check (idutilizador = auth.uid());

-- O docente precisa de ver dados dos alunos/empresas ligados aos seus estágios.
drop policy if exists "utilizador_select_estagio_participantes" on public.utilizador;
create policy "utilizador_select_estagio_participantes"
on public.utilizador for select
to authenticated
using (
    public.is_admin()
    or exists (
        select 1
        from public.estagio e
        join public.candidatura c on c.idcandidatura = e.idcandidatura
        join public.oferta_estagio o on o.idoferta = c.idoferta
        where public.can_access_estagio(e.idestagio)
          and (
              utilizador.idutilizador = c.idaluno
              or utilizador.idutilizador = e.iddocente
              or utilizador.idutilizador = e.idorientador
              or utilizador.idutilizador = o.idempresa
          )
    )
);

drop policy if exists "aluno_select_estagio_participantes" on public.aluno;
create policy "aluno_select_estagio_participantes"
on public.aluno for select
to authenticated
using (
    public.is_admin()
    or exists (
        select 1
        from public.estagio e
        join public.candidatura c on c.idcandidatura = e.idcandidatura
        where c.idaluno = aluno.idutilizador
          and public.can_access_estagio(e.idestagio)
    )
);

drop policy if exists "candidatura_select_estagio_participantes" on public.candidatura;
create policy "candidatura_select_estagio_participantes"
on public.candidatura for select
to authenticated
using (
    public.is_admin()
    or exists (
        select 1
        from public.estagio e
        where e.idcandidatura = candidatura.idcandidatura
          and public.can_access_estagio(e.idestagio)
    )
);
