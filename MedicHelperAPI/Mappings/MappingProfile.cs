using AutoMapper;
using MedicHelperAPI.DTOs;
using MedicHelperAPI.Models;

namespace MedicHelperAPI.Mappings;

public class MappingProfile : Profile
{
    public MappingProfile() 
    {
        CreateMap<RegisterModelDTO, User>()
            // FIX: Removed the manual .ForMember mapping for NormalizedEmail.
            // ASP.NET Identity's UserManager.CreateAsync() normalizes the email itself
            // using the configured ILookupNormalizer. Manually setting NormalizedEmail
            // here via AutoMapper could create a race condition or mismatch if Identity's
            // normalizer uses a different format (e.g., different Unicode normalization).
            .ForMember(dest => dest.CreatedAt, opt => opt.MapFrom(src => DateTime.UtcNow))
            .ForMember(dest => dest.UpdatedAt, opt => opt.MapFrom(src => (DateTime?)null));
        CreateMap<User, UserDTO>();

        CreateMap<Medication, MedicationDTO>();

        CreateMap<MedicationDTO, Medication>()
            .ForMember(dest => dest.CreatedAt, opt => opt.MapFrom(_ => DateTime.UtcNow)) 
            .ForMember(dest => dest.UpdatedAt, opt => opt.Ignore()); 

        CreateMap<Reminder, ReminderDTO>();

        CreateMap<ReminderDTO, Reminder>()
            .ForMember(dest => dest.CreatedAt, opt => opt.MapFrom(_ => DateTime.UtcNow))
            .ForMember(dest => dest.UpdatedAt, opt => opt.Ignore());

        CreateMap<UpdateReminderDTO, Reminder>();

        CreateMap<Note, NoteDTO>();
        CreateMap<CreateNoteDTO, Note>()
            .ForMember(dest => dest.CreatedAt, opt => opt.MapFrom(_ => DateTime.UtcNow));


        CreateMap<Appointment, AppointmentDTO>();
        CreateMap<CreateAppointmentDTO, Appointment>()
            .ForMember(dest => dest.CreatedAt, opt => opt.MapFrom(_ => DateTime.UtcNow))
            .ForMember(dest => dest.UpdatedAt, opt => opt.Ignore());

        CreateMap<Family, FamilyMemberDTO>()
                .ForMember(dest => dest.FamilyMemberId, opt => opt.MapFrom(src => src.FamilyMemberId))
                .ForMember(dest => dest.FamilyUserId, opt => opt.MapFrom(src => src.FamilyUserId))
                .ForMember(dest => dest.FirstName, opt => opt.MapFrom(src => src.FamilyUser.FirstName))
                .ForMember(dest => dest.LastName, opt => opt.MapFrom(src => src.FamilyUser.LastName))
                .ForMember(dest => dest.Email, opt => opt.MapFrom(src => src.FamilyUser.Email))
                .ForMember(dest => dest.ApprovedOn, opt => opt.MapFrom(src => src.ApprovedOn));
    }
}
