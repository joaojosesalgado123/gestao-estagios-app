-- Ligix - Supabase Auth + RLS setup
-- Run this in Supabase Dashboard > SQL Editor after checking current test data.

-- Passwords belong to Supabase Auth, not to public.utilizador.
alter table public.utilizador
drop column if exists password;

-- The application expects these public IDs to be the same UUID as auth.users.id.
-- If these ALTERs fail, remove/migrate old test rows that do not contain valid UUIDs.
alter table public.utilizador alter column idutilizador type uuid using idutilizador::uuid;
alter table public.aluno alter column idutilizador type uuid using idutilizador::uuid;
alter table public.docente alter column idutilizador type uuid using idutilizador::uuid;
alter table public.empresa alter column idutilizador type uuid using idutilizador::uuid;
alter table public.candidatura alter column idaluno type uuid using idaluno::uuid;
alter table public.oferta_estagio alter column idempresa type uuid using idempresa::uuid;
alter table public.mensagem alter column idremetente type uuid using idremetente::uuid;

-- If the foreign key below fails, inspect old profiles created before Supabase Auth:
--
-- select u.idutilizador, u.email, u.role
-- from public.utilizador u
-- left join auth.users au on au.id = u.idutilizador
-- where au.id is null;
--
-- For a development/test database, the cleanest fix is usually to remove old
-- public data and re-register users through the app so auth.users becomes the
-- source of truth. Do not run these deletes if you need to preserve existing data:
--
-- delete from public.item_avaliacao;
-- delete from public.avaliacao;
-- delete from public.relatorio_final;
-- delete from public.presenca;
-- delete from public.atividade;
-- delete from public.mensagem;
-- delete from public.conversa;
-- delete from public.estagio;
-- delete from public.candidatura;
-- delete from public.oferta_estagio;
-- delete from public.orientador_empresa;
-- delete from public.aluno;
-- delete from public.docente;
-- delete from public.empresa;
-- delete from public.utilizador;

do $$
begin
    if not exists (
        select 1
        from pg_constraint
        where conname = 'utilizador_auth_fk'
    ) then
        alter table public.utilizador
        add constraint utilizador_auth_fk
        foreign key (idutilizador)
        references auth.users(id)
        on delete cascade;
    end if;
end $$;

create or replace function public.handle_new_auth_user()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
    metadata jsonb := coalesce(new.raw_user_meta_data, '{}'::jsonb);
    profile_role text := coalesce(metadata->>'role', 'aluno');
begin
    insert into public.utilizador (
        idutilizador,
        username,
        nome,
        email,
        role
    )
    values (
        new.id,
        coalesce(nullif(metadata->>'username', ''), split_part(new.email, '@', 1)),
        coalesce(metadata->>'nome', ''),
        new.email,
        profile_role
    )
    on conflict (idutilizador) do update
    set username = excluded.username,
        nome = excluded.nome,
        email = excluded.email,
        role = excluded.role;

    if profile_role = 'aluno' then
        insert into public.aluno (
            idutilizador,
            numero_aluno,
            curso,
            telemovel
        )
        values (
            new.id,
            coalesce(metadata->>'numero_aluno', ''),
            coalesce(metadata->>'curso', ''),
            nullif(metadata->>'telemovel', '')
        )
        on conflict (idutilizador) do update
        set numero_aluno = excluded.numero_aluno,
            curso = excluded.curso,
            telemovel = excluded.telemovel;
    elsif profile_role = 'docente' then
        insert into public.docente (
            idutilizador,
            telemovel,
            area
        )
        values (
            new.id,
            nullif(metadata->>'telemovel', ''),
            nullif(metadata->>'area', '')
        )
        on conflict (idutilizador) do update
        set telemovel = excluded.telemovel,
            area = excluded.area;
    elsif profile_role = 'empresa' then
        insert into public.empresa (
            idutilizador,
            nipc,
            morada,
            descricao,
            status
        )
        values (
            new.id,
            nullif(metadata->>'nipc', ''),
            nullif(metadata->>'morada', ''),
            nullif(metadata->>'descricao', ''),
            coalesce(nullif(metadata->>'status', ''), 'pendente')
        )
        on conflict (idutilizador) do update
        set nipc = excluded.nipc,
            morada = excluded.morada,
            descricao = excluded.descricao,
            status = excluded.status;
    end if;

    return new;
end;
$$;

drop trigger if exists on_auth_user_created on auth.users;

create trigger on_auth_user_created
after insert on auth.users
for each row execute function public.handle_new_auth_user();

create or replace function public.current_profile_role()
returns text
language sql
stable
security definer
set search_path = public
as $$
    select role
    from public.utilizador
    where idutilizador = auth.uid()
$$;

create or replace function public.is_admin()
returns boolean
language sql
stable
security definer
set search_path = public
as $$
    select coalesce(public.current_profile_role() = 'admin', false)
$$;

create or replace function public.can_access_estagio(estagio_id uuid)
returns boolean
language sql
stable
security definer
set search_path = public
as $$
    select exists (
        select 1
        from public.estagio e
        join public.candidatura c on c.idcandidatura = e.idcandidatura
        join public.oferta_estagio o on o.idoferta = c.idoferta
        where e.idestagio = estagio_id
          and (
              c.idaluno = auth.uid()
              or e.iddocente = auth.uid()
              or e.idorientador = auth.uid()
              or o.idempresa = auth.uid()
              or public.is_admin()
          )
    )
$$;

alter table public.utilizador enable row level security;
alter table public.aluno enable row level security;
alter table public.docente enable row level security;
alter table public.empresa enable row level security;
alter table public.oferta_estagio enable row level security;
alter table public.candidatura enable row level security;
alter table public.estagio enable row level security;
alter table public.atividade enable row level security;
alter table public.presenca enable row level security;
alter table public.conversa enable row level security;
alter table public.mensagem enable row level security;
alter table public.avaliacao enable row level security;
alter table public.item_avaliacao enable row level security;
alter table public.relatorio_final enable row level security;

drop policy if exists "utilizador_select_own_or_admin" on public.utilizador;
create policy "utilizador_select_own_or_admin"
on public.utilizador for select
to authenticated
using (idutilizador = auth.uid() or public.is_admin());

drop policy if exists "utilizador_update_own" on public.utilizador;
create policy "utilizador_update_own"
on public.utilizador for update
to authenticated
using (idutilizador = auth.uid())
with check (idutilizador = auth.uid());

drop policy if exists "aluno_select_own_or_admin" on public.aluno;
create policy "aluno_select_own_or_admin"
on public.aluno for select
to authenticated
using (idutilizador = auth.uid() or public.is_admin());

drop policy if exists "aluno_update_own" on public.aluno;
create policy "aluno_update_own"
on public.aluno for update
to authenticated
using (idutilizador = auth.uid())
with check (idutilizador = auth.uid());

drop policy if exists "docente_select_own_or_admin" on public.docente;
create policy "docente_select_own_or_admin"
on public.docente for select
to authenticated
using (idutilizador = auth.uid() or public.is_admin());

drop policy if exists "empresa_select_own_or_admin" on public.empresa;
create policy "empresa_select_own_or_admin"
on public.empresa for select
to authenticated
using (idutilizador = auth.uid() or public.is_admin());

drop policy if exists "oferta_select_authenticated" on public.oferta_estagio;
create policy "oferta_select_authenticated"
on public.oferta_estagio for select
to authenticated
using (true);

drop policy if exists "empresa_manage_own_ofertas" on public.oferta_estagio;
create policy "empresa_manage_own_ofertas"
on public.oferta_estagio for all
to authenticated
using (idempresa = auth.uid() or public.is_admin())
with check (idempresa = auth.uid() or public.is_admin());

drop policy if exists "candidatura_select_participantes" on public.candidatura;
create policy "candidatura_select_participantes"
on public.candidatura for select
to authenticated
using (
    idaluno = auth.uid()
    or public.is_admin()
    or exists (
        select 1
        from public.oferta_estagio o
        where o.idoferta = candidatura.idoferta
          and o.idempresa = auth.uid()
    )
);

drop policy if exists "aluno_insert_own_candidatura" on public.candidatura;
create policy "aluno_insert_own_candidatura"
on public.candidatura for insert
to authenticated
with check (idaluno = auth.uid());

drop policy if exists "empresa_update_candidaturas_das_suas_ofertas" on public.candidatura;
create policy "empresa_update_candidaturas_das_suas_ofertas"
on public.candidatura for update
to authenticated
using (
    public.is_admin()
    or exists (
        select 1
        from public.oferta_estagio o
        where o.idoferta = candidatura.idoferta
          and o.idempresa = auth.uid()
    )
);

drop policy if exists "estagio_select_participantes" on public.estagio;
create policy "estagio_select_participantes"
on public.estagio for select
to authenticated
using (public.can_access_estagio(idestagio));

drop policy if exists "atividade_select_participantes" on public.atividade;
create policy "atividade_select_participantes"
on public.atividade for select
to authenticated
using (public.can_access_estagio(idestagio));

drop policy if exists "atividade_insert_participantes" on public.atividade;
create policy "atividade_insert_participantes"
on public.atividade for insert
to authenticated
with check (public.can_access_estagio(idestagio));

drop policy if exists "atividade_update_participantes" on public.atividade;
create policy "atividade_update_participantes"
on public.atividade for update
to authenticated
using (public.can_access_estagio(idestagio))
with check (public.can_access_estagio(idestagio));

drop policy if exists "presenca_select_participantes" on public.presenca;
create policy "presenca_select_participantes"
on public.presenca for select
to authenticated
using (public.can_access_estagio(idestagio));

drop policy if exists "presenca_insert_participantes" on public.presenca;
create policy "presenca_insert_participantes"
on public.presenca for insert
to authenticated
with check (public.can_access_estagio(idestagio));

drop policy if exists "presenca_update_participantes" on public.presenca;
create policy "presenca_update_participantes"
on public.presenca for update
to authenticated
using (public.can_access_estagio(idestagio))
with check (public.can_access_estagio(idestagio));

drop policy if exists "conversa_select_participantes" on public.conversa;
create policy "conversa_select_participantes"
on public.conversa for select
to authenticated
using (public.can_access_estagio(idestagio));

drop policy if exists "mensagem_select_participantes" on public.mensagem;
create policy "mensagem_select_participantes"
on public.mensagem for select
to authenticated
using (
    exists (
        select 1
        from public.conversa c
        where c.idconversa = mensagem.idconversa
          and public.can_access_estagio(c.idestagio)
    )
);

drop policy if exists "mensagem_insert_participantes" on public.mensagem;
create policy "mensagem_insert_participantes"
on public.mensagem for insert
to authenticated
with check (
    idremetente = auth.uid()
    and exists (
        select 1
        from public.conversa c
        where c.idconversa = mensagem.idconversa
          and public.can_access_estagio(c.idestagio)
    )
);

drop policy if exists "avaliacao_select_participantes" on public.avaliacao;
create policy "avaliacao_select_participantes"
on public.avaliacao for select
to authenticated
using (public.can_access_estagio(idestagio));

drop policy if exists "item_avaliacao_select_participantes" on public.item_avaliacao;
create policy "item_avaliacao_select_participantes"
on public.item_avaliacao for select
to authenticated
using (
    exists (
        select 1
        from public.avaliacao a
        where a.idavaliacao = item_avaliacao.idavaliacao
          and public.can_access_estagio(a.idestagio)
    )
);

drop policy if exists "item_avaliacao_insert_self" on public.item_avaliacao;
create policy "item_avaliacao_insert_self"
on public.item_avaliacao for insert
to authenticated
with check (idavaliador = auth.uid());

drop policy if exists "relatorio_select_participantes" on public.relatorio_final;
create policy "relatorio_select_participantes"
on public.relatorio_final for select
to authenticated
using (public.can_access_estagio(idestagio));

drop policy if exists "relatorio_insert_participantes" on public.relatorio_final;
create policy "relatorio_insert_participantes"
on public.relatorio_final for insert
to authenticated
with check (public.can_access_estagio(idestagio));
