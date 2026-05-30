-- Ligix - cria automaticamente o estagio quando uma candidatura e aceite.
-- Executar uma vez no Supabase Dashboard > SQL Editor.

-- Cada candidatura aceite deve originar, no maximo, um estagio.
do $$
begin
    if exists (
        select 1
        from public.estagio
        group by idcandidatura
        having count(*) > 1
    ) then
        raise exception
            'Existem candidaturas com mais de um estagio. Corrija os duplicados antes de instalar o trigger.';
    end if;
end;
$$;

create unique index if not exists estagio_idcandidatura_unique_idx
on public.estagio (idcandidatura);

create or replace function public.criar_estagio_ao_aceitar_candidatura()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
    if new.status = 'aceite' then
        insert into public.estagio (
            idestagio,
            status,
            idcandidatura
        )
        values (
            gen_random_uuid(),
            'ativo',
            new.idcandidatura
        )
        on conflict (idcandidatura) do nothing;
    end if;

    return new;
end;
$$;

drop trigger if exists candidatura_aceite_cria_estagio on public.candidatura;

create trigger candidatura_aceite_cria_estagio
after insert or update of status on public.candidatura
for each row
execute function public.criar_estagio_ao_aceitar_candidatura();

-- Preenche candidaturas que ja estavam aceites antes da instalacao do trigger.
insert into public.estagio (
    idestagio,
    status,
    idcandidatura
)
select
    gen_random_uuid(),
    'ativo',
    c.idcandidatura
from public.candidatura c
where c.status = 'aceite'
on conflict (idcandidatura) do nothing;
