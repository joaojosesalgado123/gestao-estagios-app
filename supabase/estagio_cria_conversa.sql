-- Ligix - cria a conversa de grupo quando nasce um estagio.
-- Executar uma vez no Supabase Dashboard > SQL Editor.

-- A app apresenta uma conversa de grupo por estagio.
do $$
begin
    if exists (
        select 1
        from public.conversa
        group by idestagio
        having count(*) > 1
    ) then
        raise exception
            'Existem estagios com mais de uma conversa. Corrija os duplicados antes de instalar o trigger.';
    end if;
end;
$$;

create unique index if not exists conversa_idestagio_unique_idx
on public.conversa (idestagio);

create or replace function public.criar_conversa_ao_criar_estagio()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
    insert into public.conversa (
        idconversa,
        idestagio,
        data_criacao,
        estado
    )
    values (
        gen_random_uuid(),
        new.idestagio,
        now(),
        'ativa'
    )
    on conflict (idestagio) do nothing;

    return new;
end;
$$;

drop trigger if exists estagio_cria_conversa on public.estagio;

create trigger estagio_cria_conversa
after insert on public.estagio
for each row
execute function public.criar_conversa_ao_criar_estagio();

-- Preenche estagios criados antes da instalacao do trigger.
insert into public.conversa (
    idconversa,
    idestagio,
    data_criacao,
    estado
)
select
    gen_random_uuid(),
    e.idestagio,
    now(),
    'ativa'
from public.estagio e
on conflict (idestagio) do nothing;
